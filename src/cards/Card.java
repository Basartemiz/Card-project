package cards;
//card class wchich implements comparable interface and get compared with maximal health, then minimal attack, then entry order
public class Card implements Comparable<Card>{
    public String name;//name of the card

    public int Ainit; //initial attack
    public int Abase; //base attack
    public int Acur; //current attack

    public int Hinit; //initial health
    public int Hbase; //base health
    public int Hcur; //current health

    public int Hmissing; //missing health for discarded cards
    public int entryOrder;      // For tie-breaking

    public Card(String name, int attack, int health, int entryOrder) {
        this.name = name;
        this.Ainit = attack;
        this.Abase = attack;
        this.Acur = attack;
        this.Hinit = health;
        this.Hbase = health;
        this.Hcur = health;

        this.entryOrder = entryOrder;
    }
    //function to get the missing health of the card
    
    //function to check if the card is alive
    public boolean isAlive() {
        return Hcur > 0;
    }
    //function to attack another card
    public void attack(Card opponent) {
        opponent.Hcur -= this.Acur;
        if (opponent.Hcur < 0) {
            opponent.Hcur = 0;
        }
    }
    
    public void defend(int damage) {
        this.Hcur -= damage;
        if (this.Hcur < 0) {
            this.Hcur = 0;
        }
    }
    public void battle(Card opponent) {
        this.attack(opponent);
        opponent.attack(this);
        if (!this.isAlive()) {
            this.Hcur = 0;
        }
        if (!opponent.isAlive()) {
            opponent.Hcur = 0;  }
        update_Acur();
    }
    private void update_Acur(){
        this.Acur = (int)Math.max(1,Math.floor( (Abase * Hcur) / Hbase));
    }
    @Override
    public int compareTo(Card other) {
       if(this.Acur!=other.Acur){
        return Integer.compare(this.Acur, other.Acur);
       }
       else {
        if(this.Hcur!=other.Hcur){
            return Integer.compare(this.Hcur, other.Hcur);
        }
        else{
            return Integer.compare(other.entryOrder, this.entryOrder);
       }
       
    }
}
 public int getAcur(){
        return this.Acur;
    }
    public int getHcur(){
        return this.Hcur;
    }   
    public int getEntryOrder(){
        return this.entryOrder;
    }
}

