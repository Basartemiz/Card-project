public class Best<Card> {
    public Card node;     // the current best node or object
    public boolean found;  // optional flag to mark if a valid result was found

    public Best() {
        this.node = null;
        this.found = false;
    }

    public Best(Card node) {
        this.node = node;
        this.found = (node != null);
    }

    // Utility to reset
    public void clear() {
        this.node = null;
        this.found = false;
    }

    public boolean isEmpty() {
        return this.node == null;
    }

    public void update(Card newNode) {
        this.node = newNode;
        this.found = (newNode != null);
    }
}