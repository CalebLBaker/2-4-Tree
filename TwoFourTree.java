package termproject;

import java.util.Random;

/**
 * Title:        Term Project 2-4 Trees
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */
public class TwoFourTree
        implements Dictionary {

    private Comparator treeComp;
    private int size = 0;
    private TFNode treeRoot = null;

    public TwoFourTree(Comparator comp) {
        treeComp = comp;
    }

    private TFNode root() {
        return treeRoot;
    }

    private void setRoot(TFNode root) {
        treeRoot = root;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Searches dictionary to determine if key is present
     * @param key to be searched for
     * @return object corresponding to key; null if not found
     */
    public Object findElement(Object key) {
        Object returned = null;
        
        // Search for key 
        TFNode node = search(key);      
        int index = FFGTE(key, node);   
        
        // If key is there, set it equal to the item to be returned 
	if (!(index == node.getNumItems()) && treeComp.isEqual(((Item) node.getItem(index)).key(), key)) {
            returned = node.getItem(index).element();
	}
        
        return returned;
    }

    /**
     * Inserts provided element into the Dictionary
     * @param key of object to be inserted
     * @param element to be inserted
     */
    @Override
    public void insertElement(Object key, Object element) {
        
        // Find in which node new element should be inserted.
        TFNode insertPoint = search(key);
        
        // If tree is empty, add new root.
        if (insertPoint == null) {
        
            setRoot(new TFNode());
            insertPoint = treeRoot;
        }
        // If key was found in internal node, find in order successor.
        else if (isInternal(insertPoint)) {
        
            insertPoint = inOrderSuccessor(FFGTE(key, insertPoint), insertPoint);
        }
        
        // Insert item into appropriate location in external node.
        insertPoint.insertItem(FFGTE(key, insertPoint), new Item(key, element));
        
        // While current node is overflowing.
        while (insertPoint.getNumItems() == 4) {
        
            TFNode parent;

            // Get parent of current node.
            if (insertPoint.getParent() != null) {
            
                parent = insertPoint.getParent();
            }
            // Create new root if current node is root.
            else {
            
                parent = new TFNode();
                setRoot(parent);
                insertPoint.setParent(parent);
                parent.setChild(0, insertPoint);
            }
            
            // Split current node and update all necessary pointers.
            split(parent, insertPoint);
            
            // Loop again if parent of current node if now overflowing.
            insertPoint = parent;
        }
        
        // Increment size.
        size++;
    }

    /**
     * Searches dictionary to determine if key is present, then
     * removes and returns corresponding object
     * @param key of data to be removed
     * @return object corresponding to key
     * @exception ElementNotFoundException if the key is not in dictionary
     */
    public Object removeElement(Object key) throws ElementNotFoundException {
        
        // Find the item to remvoe.
        TFNode node = search(key);      
        int index = FFGTE(key, node);   
        
        // Make sure that the item is there.
	if (index == node.getNumItems() || !treeComp.isEqual(((Item) node.getItem(index)).key(), key)) {
            throw new ElementNotFoundException();
	}   // end - if (index == node.getNumItems() || !treeComp.isEqual)
        
        Item toBeReturned;
        TFNode leaf = node;
        
        // If internal
        if (isInternal(node)) {
            // Find in order successor of item.
            leaf = inOrderSuccessor(index, node);
            int leafIndex = FFGTE(key, leaf);
            
            // Replace item with the removed in-order-successor.
            toBeReturned = node.replaceItem(index, leaf.removeItem(leafIndex));
        }   // end - if (isInternal(node))
        
        // If external
        else {
            toBeReturned = node.removeItem(index);
        }   // end - else
        
        underflow(leaf);    // Check for and fix underflow.
        size--;             // Decrement size.
        return toBeReturned.element();
    }   // end public Object removeElement(Object key) 
    
    /**
     * Finds the inorder successor of an element.
     * @param index is the index of the element in curr
     * @param curr is the node containing the element
     * @return the inOrderSuccessor of curr.getItem(index)
     */
    private TFNode inOrderSuccessor(int index, TFNode curr) {
        TFNode parent = curr;   // Variable to walk one behind cur
        
        // Go one right and then left as far as you can go.
        for (TFNode cur = curr.getChild(index + 1); cur != null; cur = cur.getChild(0)) {
            parent = cur;
        }   // end - for (cur = child; cur != null; cur = child)
        return parent;
    }   // end - private TFNode inOrderSuccessor(int index, TFNode)
    
    /**
     * Finds the first value in a node greater than or equal to a key
     * @param key is the key that values are compared against.
     * @param node is the node being searched.
     * @return the index of the first item greater than or equal to key.
     *  returns -1 if key is larger than any existing item.
     */
    private int FFGTE(Object key, TFNode node) {
        
        int i;
                
        // Loop across the all items in the node and return correct index.
	for (i = 0; i < node.getNumItems(); i++) {
            if (!treeComp.isLessThan(node.getItem(i).key(), key)) {
		return i;
            }   // end - if (!treeComp.isLessThan(node.getItem(i).key(), key))
	}   // end - for (i = 0; i < node.getNumItems(); i++)
        
        // Return -1 if search failed.
	return i;
    }   // end - private int FFGTE(Object key, TFNode node)
    
    /**
     * Searches for a key in the tree
     * @param key is the key that is being searched for.
     * @return the node that contains key.
     *  returns the leaf where the key would be if the key was not found.
     */
    private TFNode search(Object key) {
	TFNode parent = null;
        
        // Itterate down the tree until null is hit.
        TFNode curr = treeRoot;
	while (curr != null) {

            int i = FFGTE(key, curr);   // Find keys position in current node
            
            // If key is there, return.
            if (i != curr.getNumItems() && treeComp.isEqual(curr.getItem(i).key(), key)) {
		return curr;
            }   // end - if (curr.getItem(i).key() == key)
            
            parent = curr;
            curr = curr.getChild(i);    // Continue to next node.
	}   // end - for (curr = root; curr != null; parent = curr.parent)
        return parent;
    }   // end - TFNode search(Object key)

    /**
     * Identifies what position a node has in its parent's child array.
     * @param curr is the node being examined
     * @return the index of curr in its parent's child array
     * @exception TwoFourTreeException if curr's parent disowned it.
     */
    private int WCIT(TFNode curr) throws TwoFourTreeException {
        // If this method has a chance of being used on the root, then code
        // needs to be added to check whether curr.getParent() is null.
        TFNode par = curr.getParent();
        for (int i = 0; i <= par.getNumItems(); i++) {
            if (curr == par.getChild(i)) {
                return i;
            }   // end - if (curr == par.getChild(i))
        }   // end - for (int i = 0; i <= par.getNumItems(); i++)
        throw new TwoFourTreeException("WCIT: Parent/child confusion.");
    }   // end - private int WCIT (TFNode curr) throws TwoFourTreeException
    
    /**
     * Checks for and fixes underflow on a TFNode.
     * @param cur is the node suspected of underflow
     */
    private void underflow(TFNode cur) {
        
        // Itterate up the tree until a node is reached that didn't underflow.
        for (TFNode curr = cur; curr.getNumItems() == 0; curr = curr.getParent()) {
            if (curr == treeRoot) {
                treeRoot = curr.getChild(0);
                if (treeRoot != null) {
                    treeRoot.setParent(null);
                }
                return;
            }   // end - if (curr == treeRoot)
            
            // Define some useful variables.
            int wc = WCIT(curr);
            TFNode parent = curr.getParent();
            TFNode lSib = null;
            if (wc != 0) lSib = parent.getChild(wc - 1);
            TFNode rSib = parent.getChild(wc + 1);
        
            // Left Transfer.
            if (wc != 0 && lSib.getNumItems() > 1) {
                
                // Add item from parent to curr.
                curr.insertItem(0, parent.getItem(wc - 1));
                
                // Make curr adopt nephew.
                TFNode nephew = lSib.getChild(lSib.getNumItems());
                curr.setChild(0, nephew);
                if (nephew != null) {
                    nephew.setParent(curr);
                }   // end - if (nephew != null
                
                // Move item from sibling to parent.
                parent.replaceItem(wc - 1, lSib.deleteItem(lSib.getNumItems() - 1));
                lSib.setChild(lSib.getNumItems() + 1, null);
            }   // end - if (wc != 0 && lSib.getNumItems() > 1)
        
            // Right Transfer.
            else if (wc < parent.getNumItems() && rSib.getNumItems() > 1) {
                
                // Add item from parent to curr.
                curr.addItem(0, parent.getItem(wc));
                
                // Make curr adopt nephew.
                TFNode nephew = rSib.getChild(0);
                curr.setChild(1, nephew);
                if (nephew != null) {
                    nephew.setParent(curr);
                }   // end - if (nephew != null)
                
                // Move item from sibling to parent.
                parent.replaceItem(wc, rSib.removeItem(0));
            }   // end - else if (rigth transfer conditions)
        
            // Left fusion.
            else if (wc != 0) {
                fusion(wc, 1, curr, lSib);
            }   // end - else if (wc != 0)
            
            // Right fusion.
            else {
                fusion(wc, 0, curr, rSib);
            }   // end - else
        }   // end - for (curr.getNumItems() == 0; curr = curr.getParent())
    }   // end - private void underflow(TFNode cur)
    
    /**
     * Method that performs a left or right fusion
     * @param wc is the result of WCIT(curr).
     * @param index is 1 for a left fusion and 0 for a right fusion.
     * @param curr is the underflowed node.
     * @param sib is the sibling to be fused with curr.
     */
    private void fusion(int wc, int index, TFNode curr, TFNode sib) {
        
        TFNode parent = curr.getParent();   // Get a parent variable.
        parent.setChild(wc, sib);           // Adjust parent's child pionter.
        
        // Insert item from parent into sibling.
        sib.insertItem(index, parent.removeItem(wc - index));
        
        // Adjust index if doing a left fusion.
        if (index == 1) {
            index = sib.getNumItems();
        }   // end - if (index == 1)
        
        // Make sibling adopt the current kid's child.
        TFNode kid = curr.getChild(0);
        sib.setChild(index, kid);
        if (kid != null) {
            kid.setParent(sib);
        }   // end - if (kid != null)
    }   // end - private void fusion(int wc, int index, TFNode curr, sib)
    
     public static void main(String[] args) {

	Comparator myComp = new IntegerComparator();
        TwoFourTree myTree = new TwoFourTree(myComp);
        myTree.printAllElements();
        myTree.checkTree();
        
        int bound = 0x1000;
        int numItems = 0x100000;
        int seed = 59373;
        Random generator = new Random(seed);

        Integer ints[] = new Integer[numItems];
        
        for (int i = 0; i < numItems; i++) {
        
            ints[i] = generator.nextInt(bound);
            myTree.insertElement(ints[i], ints[i]);
            if (i < 16) {
                myTree.printAllElements();
            }
        }
        
        myTree.printAllElements();
        myTree.checkTree();
        
        for (int i = 0; i < numItems; i++) {
        
            myTree.removeElement(ints[i]);
            if (i > numItems - 16) {
                myTree.printAllElements();
            }
        }
        
        myTree.printAllElements();
        myTree.checkTree();
     }
    
    public void printAllElements() {
        System.out.println();
        int indent = 0;
        if (root() == null) {
            System.out.println("The tree is empty");
        }
        else {
            printTree(root(), indent);
        }
        System.out.println();
    }

    public void printTree(TFNode start, int indent) {
        if (start == null) {
            return;
        }
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
        printTFNode(start);
        indent += 4;
        int numChildren = start.getNumItems() + 1;
        for (int i = 0; i < numChildren; i++) {
            printTree(start.getChild(i), indent);
        }
    }

    public void printTFNode(TFNode node) {
        int numItems = node.getNumItems();
        for (int i = 0; i < numItems; i++) {
            System.out.print(((Item) node.getItem(i)).element() + " ");
        }
        System.out.println();
    }

    // checks if tree is properly hooked up, i.e., children point to parents
    public void checkTree() {
        checkTreeFromNode(treeRoot);
    }

    private void checkTreeFromNode(TFNode start) {
        if (start == null) {
            return;
        }

        if (start.getParent() != null) {
            TFNode parent = start.getParent();
            int childIndex = 0;
            for (childIndex = 0; childIndex <= parent.getNumItems(); childIndex++) {
                if (parent.getChild(childIndex) == start) {
                    break;
                }
            }
            // if child wasn't found, print problem
            if (childIndex > parent.getNumItems()) {
                System.out.println("Child to parent confusion");
                printTFNode(start);
            }
        }

        if (start.getChild(0) != null) {
            for (int childIndex = 0; childIndex <= start.getNumItems(); childIndex++) {
                if (start.getChild(childIndex) == null) {
                    System.out.println("Mixed null and non-null children");
                    printTFNode(start);
                }
                else {
                    if (start.getChild(childIndex).getParent() != start) {
                        System.out.println("Parent to child confusion");
                        printTFNode(start);
                    }
                    for (int i = childIndex - 1; i >= 0; i--) {
                        if (start.getChild(i) == start.getChild(childIndex)) {
                            System.out.println("Duplicate children of node");
                            printTFNode(start);
                        }
                    }
                }

            }
        }

        int numChildren = start.getNumItems() + 1;
        for (int childIndex = 0; childIndex < numChildren; childIndex++) {
            checkTreeFromNode(start.getChild(childIndex));
        }

    }
    
    /**
     * Check whether a given TFNode is internal.
     * @param node TFNode to test
     * @return whether 'node' is internal
     */
    private boolean isInternal(TFNode node) {
        
        // Does the node have any children to check?
        if (node.getNumItems() > 0) {
        
            // If the first child is not null, node is internal.
            return node.getChild(0) != null;
        }
        // If not, node is not internal.
        else {
        
            return false;
        }
    }
    
    /**
     * Given a parent and an overflowing child node, 
     * perform a split.
     * @param parent parent node
     * @param original overflowing child node
     */
    private void split(TFNode parent, TFNode original) {
    
        // Find where to insert 3rd element of original into parent.
        int insertLoc = WCIT(original);
        
        // Insert that element.
        parent.insertItem(insertLoc, original.getItem(2));
        
        // New node to split from original.
        TFNode newNode = new TFNode();
        // Make the 4th element of original the 1st element of original.
        newNode.addItem(0, original.getItem(3));
        // Copy newNode's 1st and 2nd children over from original's
        // 4th and 5th children, respectively.
        newNode.setChild(0, original.getChild(3));
        newNode.setChild(1, original.getChild(4));
        // Update children's parents to be newNode.
        if (newNode.getChild(0) != null) {
            
            newNode.getChild(0).setParent(newNode);
        }
        if (newNode.getChild(1) != null) {
            
            newNode.getChild(1).setParent(newNode);
        }
        // Update parent.
        newNode.setParent(parent);
        
        // Update original node.
        // Delete relocated 3rd and 4th elements.
        original.deleteItem(3);
        original.deleteItem(2);
        // Null unneeded 4th and 5th children.
        original.setChild(4, null);
        original.setChild(3, null);
        // Update parent.
        original.setParent(parent);
        
        // Update appropriate children of parent node.
        parent.setChild(insertLoc, original);
        parent.setChild(insertLoc + 1, newNode);
    }
}