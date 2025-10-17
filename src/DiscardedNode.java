
public class DiscardedNode {
        public DiscardedCard data;
        public DiscardedNode leftChild;
        public DiscardedNode rightChild;
        public DiscardedNode parent;
        int height; //assign the height of the tree

        public int min_h; //assign the minimum health in the subtree
        public int min_a; //assign the minimum attack in the subtree
        public int max_a; //assign the maximum attack in the subtree
        public int max_h; //assign the maximum health in the subtree

        public DiscardedNode(DiscardedCard data, DiscardedNode leftChild, DiscardedNode rightChild, DiscardedNode parent)
        {
            this.data=data;
            this.leftChild=leftChild;
            this.rightChild=rightChild;
            this.parent=parent;
            this.height=0; //initially height is 0 when node is created

            this.min_h=data.Hcur; //initially set the min health to max value
            this.min_a=data.Acur; //initially set the min attack to max value
            this.max_a=data.Acur; //initially set the max attack to min value
            this.max_h=data.Hcur; //initially set the max health to min value
        }


}