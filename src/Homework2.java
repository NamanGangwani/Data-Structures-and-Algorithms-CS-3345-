/*
 	Name: Naman Gangwani
 	NetID: nkg160030
 	Class: CS 3345.501
 	Date: September 24, 2017
 */

import java.io.*;
import java.util.*;

public class Homework2 {
	public static void main(String[] args) throws IOException
	{
		
		AVLTree tree = new AVLTree();
		
		Scanner input = new Scanner(new File("input.txt")); // Scans input file
		while (input.hasNextLine()) // While there are more lines to read
		{
			String line = input.nextLine();
			int ISBN = Integer.parseInt(line.substring(0, line.indexOf(" "))); // Reads in ISBN number
			String title = line.substring(line.indexOf(" ") + 1); // Rest of the line is the title
						
			AVLNode node = new AVLNode(ISBN, new Book(ISBN, title)); // Creates a new AVLNode with given information
			
			tree.insert(node); // Inserts AVL Node into tree
		}
		
		input.close();
		
		System.out.println();
		
		/* For the bonus */
		RandomBinaryTree bTree = new RandomBinaryTree();
		int numNodes = generateRandomNumber(2, 7); // Determines the number of nodes (between 2 and 7)
		
		// Generates and inserts those nodes with values between -100 and 100
		for (int i = 0; i < numNodes; i++)
			bTree.insert(new Node(generateRandomNumber(-100, 100)));
		
		if (bTree.isBST())
			if (bTree.isBalanced())
				System.out.println("Randomly generated binary tree is a BST and also an AVL tree.");
			else
				System.out.println("Randomly generated binary tree is a BST but not an AVL tree.");
		else
			System.out.println("Randomly generated binary tree is not a BST and therefore also not an AVL tree.");
	}
	
	/* Method for getting a random number between the specified min and max values */
	public static int generateRandomNumber(int min, int max)
	{
		return (int) Math.ceil(Math.random() * (max - min) + min);
	}
}

class AVLTree
{
	public AVLNode root;
	
	/* Public method to insert an AVLNode by just passing in the node itself */
	public void insert(AVLNode n) { insert(n, root, null, n.key); }
	
	/* Helper recursive method to add a node based on the numerical value of its ISBN into the AVLTree */
	private void insert(AVLNode n, AVLNode curr, AVLNode prev, int forISBN)
	{
		if (root == null) // If there is no root yet
			root = n; // This node will be the root
		else
			if (n.key < curr.key) // If the new node needs to go left of the current node
			{
				if (curr.leftPtr == null) // If the current node doesn't have anything to its left
					curr.leftPtr = n; // New node is at current node's left
				else
					insert(n, curr.leftPtr, curr, forISBN); // Keep searching left
				
				rotateIfNecessary(curr, prev, forISBN); // Rotate from the current node if an imbalance was created
			}
			else if (n.key > curr.key) // If the new node needs to go right of the current node
			{
				if (curr.rightPtr == null) // If the current node doesn't have anything to its right
					curr.rightPtr = n; // New node is at current node's right
					
				else
					insert(n, curr.rightPtr, curr, forISBN); // Keep searching right
				
				rotateIfNecessary(curr, prev, forISBN); // Rotate from the current node if an imbalance was created
			}
	}
	
	/* Method to determine whether the current node needs to be rotated */
	private void rotateIfNecessary(AVLNode n, AVLNode prev, int forISBN)
	{
		n.updateHeight(); // Updates height first
		
		if (n.isUnbalanced()) // If the current node is unbalanced
		{
			// Determines whether the left or right side is bigger
			int left = 0, right = 0;
			if (n.leftPtr != null) left = n.leftPtr.height;
			if (n.rightPtr != null) right = n.rightPtr.height;
			
			//System.out.println("Base of rotation: "+n.key);
			if (left > right)
			{
				// Checks whether a double rotation is needed
				if (n.leftPtr.rightPtr != null && (n.leftPtr.leftPtr == null || n.leftPtr.rightPtr.height > n.leftPtr.leftPtr.height))
				{
					rotate(n.leftPtr, n, -1); // Rotate child node left first
					System.out.println("Imbalance occurred at inserting "+forISBN+"; fixed in LeftRight Rotation");
				} 
				else
					System.out.println("Imbalance occurred at inserting "+forISBN+"; fixed in Right Rotation");
				rotate(n, prev, 1); // Rotate right
			}
			else
			{
				// Checks whether a double rotation is needed
				if (n.rightPtr.leftPtr != null && (n.rightPtr.rightPtr == null || n.rightPtr.leftPtr.height > n.rightPtr.rightPtr.height))
				{
					rotate(n.rightPtr, n, 1); // Rotate child node right first
					System.out.println("Imbalance occurred at inserting "+forISBN+"; fixed in RightLeft Rotation");
				}
				else
					System.out.println("Imbalance occurred at inserting "+forISBN+"; fixed in Left Rotation");
				rotate(n, prev, -1); // Rotate left
			}
		}
	}
	
	/* Method to rotate a node based on a given direction; prev is reserved for maintaining pointer links */
	public void rotate(AVLNode n, AVLNode prev, int dir)
	{
		if (dir == -1) // Left
		{
			// Performs left rotation
			AVLNode child = n.rightPtr;
			n.rightPtr = child.leftPtr;
			child.leftPtr = n;
			
			if (prev != null && prev.leftPtr == n)
				prev.leftPtr = child;
			if (prev != null && prev.rightPtr == n)
				prev.rightPtr = child;
			
			// Updates heights
			child.updateHeight();
			n.updateHeight();
			
			// If the base of the rotation was the root, update the root
			if (n == root)
				root = child;
		}
		else if (dir == 1) // Right
		{
			// Performs right rotation
			AVLNode child = n.leftPtr;
			n.leftPtr = child.rightPtr;
			child.rightPtr = n;
			
			if (prev != null && prev.leftPtr == n)
				prev.leftPtr = child;
			if (prev != null && prev.rightPtr == n)
				prev.rightPtr = child;
			
			// Updates heights
			child.updateHeight();
			n.updateHeight();
			
			// If the base of the rotation was the root, update the root
			if (n == root)
				root = child;
		}
	}
}

class AVLNode
{
	int key; // ISBN number
	Book value;
	int height;
	AVLNode leftPtr;
	AVLNode rightPtr;
	
	/* Constructor*/
	public AVLNode(int key, Book value)
	{
		this.key = key;
		this.value = value;
	}
	
	/* Function to update the height of the node */
	public void updateHeight() { this.height = updateHeight(this) - 1; }
	
	/* Helper function that traverses through AVLNode's children to get its height */
	private int updateHeight(AVLNode node)
	{
		if (node == null)
			return 0;
		else
		{
			int maxHeight = Math.max(updateHeight(node.leftPtr), updateHeight(node.rightPtr));
			return maxHeight + 1;
		}
	}
	
	/* Method for determining whether the AVLNode is unbalanced or not based on the heights of its children */
	public boolean isUnbalanced()
	{
		if (leftPtr != null && rightPtr != null) // If both left and right children exist 
			return Math.abs(leftPtr.height - rightPtr.height) > 1; // Difference > 1 between children determines imbalance
		else if (leftPtr != null && rightPtr == null) // If it's one-sided on the left side
			return leftPtr.height + 1 > 1;
		else if (leftPtr == null && rightPtr != null) // If it's one-sided on the right side
			return rightPtr.height + 1 > 1;
		else
			return false; // No imbalance detected
	}
	
	/* Getter Methods */
	public int getKey() { return key; }
	public Book getValue() {return value; }
	public int getHeight() { return height; }
	public AVLNode getLeft() {return leftPtr; }
	public AVLNode getRight() {return rightPtr; }
	
	/* Setter Methods */
	public void setKey(int key) { this.key = key; }
	public void setValue(Book value) {this.value = value; }
	public void setHeight(int height) { this.height = height; }
	public void setLeft(AVLNode leftPtr) { this.leftPtr = leftPtr; }
	public void setRight(AVLNode rightPtr) { this.rightPtr = rightPtr; }
}

class Book
{
	int ISBN;
	String title;
	
	/* Constructor */
	public Book(int ISBN, String title)
	{
		this.ISBN = ISBN;
		this.title = title;
	}
	
	/* Getter Methods */
	public int getISBN() { return ISBN; }
	public String getTitle() {return title; }
	
	/* Setter Methods */
	public void setISBN(int ISBN) { this.ISBN = ISBN; }
	public void setTitle(String title) { this.title = title; }
}

/* For the bonus*/
class Node
{
	int key, height;
	Node left, right;
	
	public Node(int key) { this.key = key; }
	
	/* Method to get the height of the node */
	public void updateHeight() { height = updateHeight(this) - 1; }
	
	/* Recursive helper method to get the height of the node */
	private int updateHeight(Node n)
	{
		if (n == null)
			return 0;
		return Math.max(updateHeight(n.left), updateHeight(n.right)) + 1; // Recurse through all the children to get the height
	}
}

/* For the bonus*/
class RandomBinaryTree
{
	Node root;
	
	/* Method to randomly insert the given node somewhere in the tree */
	public void insert(Node n)
	{
		if (root == null) // If there's no root yet, assign it to the root
			root = n;
		else
		{
			Node curr = root;
			while (true)
			{
				int direction = (int) Math.ceil(Math.random() * 10); // Random number between 1 and 10
				if (direction <= 5) // Left if it's 5 of less
					if (curr.left == null) // If there's a spot a the left
					{
						curr.left = n; // Set it to be the left child
						break;
					}
					else
						curr = curr.left; // Move on left
				else // Right if it's 6 or greater
					if (curr.right == null)
					{
						curr.right = n; // Set it to be the right child
						break;
					}
					else
						curr = curr.right; // Move on right
			}
		}
	}
	
	
	/* Returns whether this random binary tree is also a binary search tree */
	public boolean isBST() { return isBST(root); }
	
	/* Helper recursive method for determining whether this binary tree is a BST */
	private boolean isBST(Node n)
	{
		if (n == null) // If the current node doesn't exist, there's no need for comparisons
			return true;
		
		// If the left is less than the current and the right is greater than the current
		if ((((n.left != null && n.left.key < n.key) || n.left == null))
				&& ((n.right != null && n.right.key > n.key) || n.right == null))
			return (isBST(n.left) && isBST(n.right)); // Check its children too
		
		return false;
	}
	
	/* Method for determining if this random binary tree is balanced */
	public boolean isBalanced() { return isBalanced(root); }
	
	/* Recursive helper method for determining if this random binary tree is balanced */
	private boolean isBalanced(Node n)
	{
		boolean centerBalanced = false, leftBalanced = false, rightBalanced= false;
		if (n.left != null)
			leftBalanced = isBalanced(n.left); // Check if left branch is balanced first
		else
			leftBalanced = true;
		if (n.right != null)
			rightBalanced = isBalanced(n.right); // Check if right branch is balanced first
		else
			rightBalanced = true;
		
		n.updateHeight(); // Call to update current node's height
		
		if (n.left != null && n.right != null) // If both left and right children exist 
			centerBalanced = Math.abs(n.left.height - n.right.height) <= 1; // Difference > 1 between children determines imbalance
		else if (n.left != null && n.right == null) // If it's one-sided on the left side
			centerBalanced = n.left.height + 1 <= 1;
		else if (n.left == null && n.right != null) // If it's one-sided on the right side
			centerBalanced = n.right.height + 1 <= 1;
		else
			centerBalanced = true; // It is balanced
		
		return (centerBalanced && leftBalanced && rightBalanced);
	}
}
