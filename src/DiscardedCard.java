public class DiscardedCard extends Card {
    // Constructor to initialize the DiscardedCard with name, attack, health, entryOrder, and Hmissing

    int Hmissing=0;
    public DiscardedCard(String name, int attack, int health, int entryOrder,int Hmissing) {
        super(name, attack, health, entryOrder);
        this.Hmissing = Hmissing; // Set current health to missing health
    }




    public int compareTo(DiscardedCard other) {
        if (this.Hmissing != other.Hmissing) {
            return Integer.compare(this.Hmissing, other.Hmissing); // ascending Hmissing
        }
        return Integer.compare(this.entryOrder, other.entryOrder); // earliest first
    }

     public void fully_revive(){
    this.Abase = (int) Math.max(1, Math.floor(this.Abase * 0.9));

}

public void partial_revive(){
    this.Abase = (int)Math.max(1, Math.floor(this.Abase * 0.95));
}

}
