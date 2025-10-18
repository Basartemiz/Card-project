public class MainLogic {

    static int entryOrderCounter = 0; // counter to keep track of entry order
    static int discardedEntryOrderCounter = 0; // counter to keep track of entry order in discarded pile
    static int point_counter_us = 0; // counter to keep track of points
    static int point_counter_op = 0; // counter to keep track of points

    // Max H, then min A, then earliest entry
    static AvlTree<Card> deck = new AvlTree(); // define the avl tree for the deck
    static DiscardedAvlTree<DiscardedCard> discardedDeck = new DiscardedAvlTree(); // define the avl tree for the discarded deck

    MainLogic() {}

    public void add_card(String name, int attack, int health) {
        Card new_card = new Card(name, attack, health, entryOrderCounter);
        entryOrderCounter++;
        deck.insert(new_card);
    }

    public void remove_card(Card remove_card) {
        deck.remove(remove_card);
    }

    public void insert_card(Card insert_card) {
        deck.insert(insert_card);
    }

    public String draw_card(String name, int att, int hp) {
        add_card(name, att, hp);
        return "Added " + name + " to the deck";
    }

    public String battle(int att, int hp, int heal_pool) {
        Card oppCard = new Card(null, att, hp, 0);

        // case 0: deck empty
        if (deck == null || deck.isEmpty()) {
            int revived_count = revive_from_discarded(heal_pool);
            point_counter_op += 2;
            return "No cards to play, " + revived_count + " cards revived";
        }
        
        // ---------- Priority 1: survive AND kill ----------
        Card pick = pickP1(att, hp);
        if (pick != null) {
            Card c = pick;
            remove_card(c);
            c.battle(oppCard);
            calculate_score(c, oppCard);

            if (c.name.equals("small5512")) System.out.println("debug");

            if (c.Hcur < 0) {
                return String.format(
                    "Found with priority 1, Survivor plays %s, the played card is discarded, %d cards revived",
                    c.name, 0
                );
            } else {
                c.entryOrder = entryOrderCounter++;
                insert_card(c);
                int revived_count = revive_from_discarded(heal_pool);
                return String.format(
                    "Found with priority 1, Survivor plays %s, the played card returned to deck, %d cards revived",
                    c.name, revived_count
                );
            }
        }

        // ---------- Priority 2: survive but NOT kill ----------
        pick = pickP2(att, hp);
        if (pick != null) {
            Card c = pick;
            remove_card(c);
            c.battle(new Card(null, att, hp, 0));
            calculate_score(c, oppCard);

            if (c.Hcur < 0) {
                return String.format(
                    "Found with priority 2, Survivor plays %s, the played card is discarded, %d cards revived",
                    c.name, 0
                );
            } else {
                c.entryOrder = entryOrderCounter++;
                insert_card(c);
                int revived_count = revive_from_discarded(heal_pool);
                return String.format(
                    "Found with priority 2, Survivor plays %s, the played card returned to deck, %d cards revived",
                    c.name, revived_count
                );
            }
        }

        // ---------- Priority 3: kill but DON'T survive ----------
        pick = pickP3(att, hp);
        if (pick != null) {
            Card c = pick;
            remove_card(c);
            c.battle(oppCard);
            calculate_score(c, oppCard);

            if (c.Hcur <= 0) {
                discardedEntryOrderCounter++;
                DiscardedCard dc = new DiscardedCard(
                    c.name, c.Abase, c.Hbase, discardedEntryOrderCounter, c.Hbase
                );
                discardedDeck.insert(dc);
                int revived_count = revive_from_discarded(heal_pool);
                return String.format(
                    "Found with priority 3, Survivor plays %s, the played card is discarded, %d cards revived",
                    c.name, revived_count
                );
            } else {
                c.entryOrder = entryOrderCounter++;
                insert_card(c);
                int revived_count = revive_from_discarded(heal_pool);
                return String.format(
                    "Found with priority 3, Survivor plays %s, the played card returned to deck, %d cards revived",
                    c.name, revived_count
                );
            }
        }

        // ---------- Priority 4: max damage ----------
        Card card_pr4 = pickP4();
        if (card_pr4 != null) {
            Card c = card_pr4;
            if (c.name.equals("small5512"))
                System.out.println("debug");
            remove_card(c);
            c.battle(oppCard);
            calculate_score(c, oppCard);

            if (c.Hcur <= 0) {
                discardedEntryOrderCounter++;
                DiscardedCard dc = new DiscardedCard(
                    c.name, c.Abase, c.Hbase, discardedEntryOrderCounter, c.Hbase
                );
                discardedDeck.insert(dc);
                int revived_count = revive_from_discarded(heal_pool);
                return String.format(
                    "Found with priority 4, Survivor plays %s, the played card is discarded, %d cards revived",
                    c.name, revived_count
                );
            } else {
                c.entryOrder = entryOrderCounter++;
                insert_card(c);
                int revived_count = revive_from_discarded(heal_pool);
                return String.format(
                    "Found with priority 4, Survivor plays %s, the played card returned to deck, %d cards revived",
                    c.name, revived_count
                );
            }
        }

        // Defensive fallback (shouldn't happen if deck not empty)
        point_counter_op += 2;
        return "No card to play, 0 cards revived";
    }

    // helper to battle function
    public int revive_from_discarded(int heal_pool) {
        int revived_count = 0;
        while (heal_pool > 0 && discardedDeck != null && !discardedDeck.isEmpty()) { // full revive loop
            DiscardedCard dc = getHmissingBestCard(heal_pool); // get best card within heal pool
            if (dc == null) break;

            if (dc.Hmissing <= heal_pool) {
                heal_pool -= dc.Hmissing;
                discardedDeck.remove(dc);
                dc.fully_revive();
                entryOrderCounter++;
                Card c = new Card(dc.name, dc.Abase, dc.Hbase, entryOrderCounter);
                if (c.name.equals("small5512")) System.out.println("debug");
                insert_card(c);
                revived_count++;
            } else break;
        }
        // final partial revive if any heal pool left
        if (heal_pool > 0 && discardedDeck != null && !discardedDeck.isEmpty()) {
            DiscardedCard dc = getMinHmissingCard();
            discardedDeck.remove(dc);
            dc.Hmissing -= heal_pool;
            dc.partial_revive();

            discardedEntryOrderCounter++;
            dc.entryOrder = discardedEntryOrderCounter;
            heal_pool = 0;
            discardedDeck.insert(dc);
        }
        return revived_count;
    }
    // helper to get best Hmissing card within heal pool
    public DiscardedCard getHmissingBestCard(int heal_pool) {
        if (discardedDeck == null || discardedDeck.root == null) return null;

        DiscardedNode n = discardedDeck.root; // start from root
        DiscardedNode best = null; // to keep track of best candidate

        while (n != null) {
            DiscardedCard d = n.data;
            // check if this card can be healed
            if (d.Hmissing <= heal_pool) {
                if (best == null || 
                    d.Hmissing > best.data.Hmissing ||
                    (d.Hmissing == best.data.Hmissing && d.entryOrder < best.data.entryOrder)) {
                    best = n;
                }
                n = n.rightChild;
            } else {
                n = n.leftChild;
            }
        }
        return best == null ? null : best.data;
    }
    // helper to get min Hmissing card
    private DiscardedCard getMinHmissingCard() {
        if (discardedDeck == null || discardedDeck.root == null) return null;

        DiscardedNode n = discardedDeck.root;
        DiscardedCard best = null;

        while (n != null) {
            DiscardedCard d = n.data;

            if (best == null ||
                d.Hmissing < best.Hmissing ||
                (d.Hmissing == best.Hmissing && d.entryOrder < best.entryOrder)) {
                best = d;
            }
            n = n.leftChild != null ? n.leftChild : n.rightChild;
        }
        return best;
    }

    // helper function to calculate score
    private void calculate_score(Card our_card, Card oppCard) {
        if (our_card.Hcur <= 0) point_counter_op += 2;
        if (oppCard.Hcur <= 0) point_counter_us += 2;
        if (our_card.Hcur > 0 && our_card.Hcur <= our_card.Hbase) point_counter_op += 1;
        if (oppCard.Hcur > 0 && oppCard.Hcur <= oppCard.Hbase) point_counter_us += 1;
    }
    // helper to revise deck after playing a card
    private void reviseDeck(Card played_card, int heal_pool) {
        int revived_count = 0;
        if (played_card.Hcur <= 0) remove_card(played_card);
    }
    // helper to get deck count
    public static String deck_count() {
        return "Number of cards in the deck: " + deck.getSize();
    }
    // helper to get discarded pile count
    public static String discard_pile_count() {
        return "Number of cards in the discard pile: " + discardedDeck.getSize();
    }
    // helper to find winner
    public static String find_winning() {
        if (point_counter_us >= point_counter_op)
            return "The Survivor, Score: " + point_counter_us;
        else
            return "The Stranger, Score: " + point_counter_op;
    }

    public String steal_card(int att, int hp) {
        if (deck == null || deck.isEmpty()) return "No card to steal";
        Card c = pickP1(hp, att + 1);
        if (c == null) return "No card to steal";
        remove_card(c);
        return "The Stranger stole the card: " + c.name;
    }

    // ---------- Priority 1 ----------
    public Card pickP1(int Astr, int Hstr) {
        Best<Node> hit = new Best<>();
        p1_find(deck.root, Astr, Hstr, hit);
        return hit.isEmpty() ? null : hit.node.data;
    }
    // helper for priority 1
    private void p1_find(Node n, int Astr, int Hstr, Best<Node> hit) {
        if (n == null) return;
        if (n.data.name.equals("small5401")) {
            int debug = 0;
        }

        if (n.max_a < Hstr || n.max_h <= Astr) return;
        // check left child
        if (hit.isEmpty() || (n.data.Acur >= Hstr)) {
            if (n.leftChild != null && n.leftChild.max_a >= Hstr && n.leftChild.max_h > Astr) {
                p1_find(n.leftChild, Astr, Hstr, hit);
            }
        }
        // check current node
        int a = n.data.Acur, h = n.data.Hcur;
        if (a >= Hstr && h > Astr) {
            if (hit.isEmpty() ||
                a < hit.node.data.Acur ||
                (a == hit.node.data.Acur && h < hit.node.data.Hcur) ||
                (a == hit.node.data.Acur && h == hit.node.data.Hcur &&
                 n.data.entryOrder < hit.node.data.entryOrder)) {
                hit.update(n);
            }
        }
        // check right child
        if (hit.isEmpty() || (n.rightChild != null && (n.data.Acur <= hit.node.data.Acur))) {
            if (n.rightChild != null && n.rightChild.max_a >= Hstr && n.rightChild.max_h > Astr)
                p1_find(n.rightChild, Astr, Hstr, hit);
        }
    }

    // ---------- Priority 2 ----------
    public Card pickP2(int Astr, int Hstr) {
        if (deck == null || deck.root == null) return null;
        Best<Node> best = new Best<>();
        p2_find(deck.root, Astr, Hstr, best);
        return best.isEmpty() ? null : ((Card) best.node.data);
    }
    // helper for priority 2
    private void p2_find(Node n, int Astr, int Hstr, Best<Node> best) {
        if (n == null) return;
        if (n.data.name.equals("small5573")) {
            int debug = 0;
        }

        if (n.max_h <= Astr || n.min_a >= Hstr) return;
        // check right child
        if (n.rightChild != null &&
            n.rightChild.min_a < Hstr &&
            n.rightChild.max_h > Astr) {
            p2_find(n.rightChild, Astr, Hstr, best);
        }

        int a = n.data.Acur;
        int h = n.data.Hcur;
        // check current node
        if (a < Hstr && h > Astr) {
            if (best.isEmpty() ||
                a > best.node.data.Acur ||
                (a == best.node.data.Acur && h < best.node.data.Hcur) ||
                (a == best.node.data.Acur && h == best.node.data.Hcur &&
                 n.data.entryOrder < best.node.data.entryOrder)) {
                best.update(n);
            }
        }
        // check left child
        if (best.isEmpty() || (n.leftChild != null && n.data.Acur >= best.node.data.Acur)) {
            if (n.leftChild != null &&
                n.leftChild.min_a < Hstr &&
                n.leftChild.max_h > Astr) {
                p2_find(n.leftChild, Astr, Hstr, best);
            }
        }
    }

    // ---------- Priority 3 ----------
    public Card pickP3(int Astr, int Hstr) {
        if (deck == null || deck.root == null) return null;
        Best<Node> best = new Best<>();
        p3_find(deck.root, Astr, Hstr, best);
        return best.isEmpty() ? null : (Card) best.node.data;
    }
// helper for priority 3
    private void p3_find(Node n, int Astr, int Hstr, Best<Node> best) {
        if (n == null) return;

        if (n.max_a < Hstr || n.min_h > Astr) return;

        Node L = n.leftChild;
        if (L != null && L.max_a >= Hstr && L.min_h <= Astr) {
            p3_find(L, Astr, Hstr, best);
        }

        int a = n.data.Acur;//get attack
        int h = n.data.Hcur;//get health

        if (a >= Hstr && h <= Astr) { // found a candidate
            if (best.isEmpty() ||
                (a < best.node.data.Acur) ||
                (a == best.node.data.Acur && h < best.node.data.Hcur) ||
                (a == best.node.data.Acur && h == best.node.data.Hcur &&
                 n.data.entryOrder < best.node.data.entryOrder)) {
                best.update(n);
            }
        }

        Node R = n.rightChild;
        if (R != null && R.max_a >= Hstr && R.min_h <= Astr) {
            p3_find(R, Astr, Hstr, best); //find in right subtree
        }
    }

    // ---------- Priority 4 ----------
    public Card pickP4() { // find max A, then min H
        if (deck == null || deck.root == null) return null;
        int targetA = deck.root.max_a;
        Best<Node> best = new Best<>();
        p4_findMinHWithA(deck.root, targetA, best);
        return best.isEmpty() ? null : (Card) best.node.data;
    }

    private void p4_findMinHWithA(Node n, int targetA, Best<Node> best) {
        if (n == null) return;

        if (n.data.name.equals("small5512")) System.out.println("debug");
        if (n.data.name.equals("small5645")) System.out.println("debug");

        if (n.min_a > targetA || n.max_a < targetA) return;

        p4_findMinHWithA(n.leftChild, targetA, best);

        int a = n.data.Acur; //get attack
        int h = n.data.Hcur; //get health

        if (a == targetA) { // found a candidate
            if (best.isEmpty() ||
                h < best.node.data.Hcur ||
                (h == best.node.data.Hcur && n.data.entryOrder < best.node.data.entryOrder)) {
                best.update(n);
            }
        }

        p4_findMinHWithA(n.rightChild, targetA, best); //find in right subtree
    }
}