import java.util.ArrayList;
import java.util.NoSuchElementException;
;

    

public class AvlTree<Type extends Comparable<? super Type>> {
    
    //define the class variables
    private final int IMBALANCE=2; //define the imbalance factor


    public Node root; //assign the root node
    int kth=1;
    Node target; //assign the target node for finding the kth largest element
    int size=0;

    

   public AvlTree() { //
       
        this.root = null;
    }//set the root to null
    
      
    public Card find_kth_largest(int k){ //function to find the kth largest element
        
        if (this.root == null) return null;
        if (k <= 0) return null;
        if (k > size) return null;

        
        ArrayList<Node> stack = new ArrayList<>();
        Node curr = this.root;
        int count = 0;

        while (curr != null || !stack.isEmpty()) {
            // go right first (largest elements first)
            while (curr != null) {
                stack.add(curr);
                curr = curr.rightChild;
            }
            curr = stack.remove(stack.size() - 1);
            count++;
            if (count == k) {
                target = curr; // keep target assignment
                return curr.data;
            }
            curr = curr.leftChild;
        }
        return null;
    }
    public boolean isEmpty(){ //this function checks if the tree is empty
      return  this.root==null;
    }
    public void clear(){this.root=null;} //this function clears the tree by setting the root to null

    public Card findMin(){ //this function finds the minimum value in the tree
        if(this.isEmpty()){
            throw new NoSuchElementException();
        }
        else return findMin(this.root).data;
    }
    private Node findMin(Node node){ //helper function to find the minimum
        while(node.leftChild!=null){
            node=node.leftChild;
        

        }
        return node;
    }

    public Card findMax(){ //this function finds the maximum value in the tree
        if(this.isEmpty()){
            throw new NoSuchElementException();
        }
        else return findMax(this.root).data;
    }
    private Node findMax(Node node){ //helper function to find the maximum
        while(node.rightChild!=null){
            node=node.rightChild;   
        }
        return node;
    }

    public Node insert(Card new_data){

        kth=1;//reset kth to 1 for each insertion
        size++; //increment size for each insertion

        Node root_check=insert(new_data,root,null);
        
        if(root_check==null){
            System.out.println("Insertion failed");
        }
        this.root=root_check;
        return root;
    }
    private Node insert(Card new_node,Node node,Node parent){
        if(node==null){
            return new Node(new_node,null,null,parent);
        }
        int comparison=new_node.compareTo(node.data);
        if(comparison<0){
            node.leftChild=insert(new_node,node.leftChild,node); 
        }
        else if(comparison>0){
            node.rightChild=insert(new_node,node.rightChild,node);
        }  
        else{
            //duplicate do nothing
        }
        updateHeight(node);
        return make_avl_property(pull(node));} //makes avl property after insertion

    public void remove(Card data){

        kth=1;//reset kth to 1 for each removal
        size--; //decrement size for each removal

        this.root=this.remove(data,this.root);
    }
    private Node remove(Card data,Node node){
        if(node==null){
            return node;
        }
        int comparison=data.compareTo(node.data);
        if(comparison<0)
            node.leftChild=this.remove(data,node.leftChild);
        else if(comparison>0)
            node.rightChild=this.remove(data,node.rightChild);
        else if(node.rightChild!=null && node.leftChild!=null){
            node.data=this.findMin(node.rightChild).data;
            node.rightChild=this.remove(node.data,node.rightChild);
            
        }
        else if(node.leftChild!=null){
            node=node.leftChild;
            
        }
        else{
            if(node.rightChild!=null){
            node=node.rightChild;
            }
            else{
                return null;
            }
        }
        updateHeight(node);
        return make_avl_property(pull(node)); //makes avl property after deletion
    }


    //function to make the avl property
   private Node make_avl_property(Node node){
    if (node == null) return null;

    updateHeight(node); // ensure node’s height is correct before checking

    int lh = h(node.leftChild);
    int rh = h(node.rightChild);

    if (lh >= rh + 2) { // left heavy (use >=; see point 3)
        Node L = node.leftChild;
        if (h(L.leftChild) < h(L.rightChild)) {
            node.leftChild = rotateLeftChild(L); //  first rotate left on left child
        }
        node = rotateRightChild(node);
    } else if (rh >= lh + 2) { // right heavy
        Node R = node.rightChild;
        if (h(R.rightChild) < h(R.leftChild)) {
            node.rightChild = rotateRightChild(R); //  first rotate right on right child
        }
        node = rotateLeftChild(node);
    }

    
    updateHeight(node);
    return node;
}
//functions for rotations
 private Node rotateRightChild(Node y) {
        Node x = y.leftChild;
        Node T2 = x.rightChild;

        // rotate
        x.rightChild = y;
        x.parent = y.parent;
        y.parent = x;
        y.leftChild = T2;
        if (T2 != null) T2.parent = y;

        // pull (bottom-up)
        pull(y);
        pull(x);
        return x;
    }

    private Node rotateLeftChild(Node x) {
        Node y = x.rightChild;
        Node T2 = y.leftChild;

        // rotate
        y.leftChild = x;
        y.parent = x.parent;
        x.parent = y;
        x.rightChild = T2;
        if (T2 != null) T2.parent = x;

        // pull (bottom-up)
        pull(x);
        pull(y);
        return y;
    }


    private Node doubleLeftChild(Node k3){ //double rotate with left child
        

        k3.leftChild = rotateLeftChild(k3.leftChild);  // left rotation on left child (your naming) 
        return rotateRightChild(k3);
    }
    private Node doubleRightChild(Node k1){ //double rotate with right child
    
        k1.rightChild=rotateRightChild(k1.rightChild);
        return rotateLeftChild(k1);
    }



    //some helper functions 
    public int getSize(){
        return this.size;
    }


    private int h(Node n) { return (n == null) ? -1 : n.height; }
    private void updateHeight(Node n) {
    if (n != null) n.height = Math.max(h(n.leftChild), h(n.rightChild)) + 1;
    }
 private Node pull(Node n) {

        // height
        n.height = 1 + Math.max(h(n.leftChild), h(n.rightChild));

        // current node's own A/H
        int a = (n.data.Acur);
        int h = n.data.Hcur;

        // children aggregates (with sentinels)
        int left_min_h  = (n.leftChild  == null) ? Integer.MAX_VALUE : n.leftChild.min_h;
        int right_min_h = (n.rightChild == null) ? Integer.MAX_VALUE : n.rightChild.min_h;

        int left_min_a  = (n.leftChild  == null) ? Integer.MAX_VALUE : n.leftChild.min_a;
        int right_min_a = (n.rightChild == null) ? Integer.MAX_VALUE : n.rightChild.min_a;

        int left_max_a  = (n.leftChild  == null) ? Integer.MIN_VALUE : n.leftChild.max_a;
        int right_max_a = (n.rightChild == null) ? Integer.MIN_VALUE : n.rightChild.max_a;

        int left_max_h  = (n.leftChild  == null) ? Integer.MIN_VALUE : n.leftChild.max_h;
        int right_max_h = (n.rightChild == null) ? Integer.MIN_VALUE : n.rightChild.max_h;

        // subtree mins/maxs
        n.min_h = Math.min(h, Math.min(left_min_h, right_min_h));
        n.min_a = Math.min(a, Math.min(left_min_a, right_min_a));
        n.max_a = Math.max(a, Math.max(left_max_a, right_max_a));
        n.max_h = Math.max(h, Math.max(left_max_h, right_max_h));
        return n;
    }
}


