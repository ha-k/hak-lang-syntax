//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import hlt.language.util.Stack;

/**
 * This class displays a parse tree using Java Swing components.
 * 
 * @see         ParseNode
 *
 * @version     Last modified on Fri Apr 13 20:14:05 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class TreeDisplay extends JFrame
{
  ImageIcon leafIcon =
      new ImageIcon(ClassLoader.getSystemResource
                    ("resources/images/arrows/triangles/right/blue.gif"));

  ImageIcon openIcon =
      new ImageIcon(ClassLoader.getSystemResource
                    ("resources/images/arrows/triangles/right/green.gif"));

  ImageIcon closedIcon =
      new ImageIcon(ClassLoader.getSystemResource
                    ("resources/images/arrows/triangles/right/red.gif"));

  DefaultMutableTreeNode root;

  JTree tree;
  DefaultTreeModel treeModel;

  public TreeDisplay (ParseNode node)
    {
      this(node,null);
    }

  public TreeDisplay (ParseNode node, String filename)
    {
      super(filename==null ? "Parse Tree" : filename);
      
      setSize(500,5000);

      setContentPane(new ScrollPane());
      
      setDefaultCloseOperation(EXIT_ON_CLOSE);

      root = new DefaultMutableTreeNode(node);

      treeModel = new DefaultTreeModel(root);
      tree = new JTree(treeModel);

      ParseTreeRenderer renderer = new ParseTreeRenderer();

      renderer.setLeafIcon(leafIcon);
      renderer.setOpenIcon(openIcon);
      renderer.setClosedIcon(closedIcon);

      tree.setCellRenderer(renderer);

      tree.putClientProperty("JTree.lineStyle","Angled");

      fillTree();

      getContentPane().add(tree, BorderLayout.CENTER);
      setVisible(true);
    }

  public void fillTree ()
    {
      Stack treeNodes = new Stack();

      treeNodes.push(root);

      while (!treeNodes.isEmpty())
        {
          DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)treeNodes.pop();

          ParseNode parseNode = (ParseNode)treeNode.getUserObject();
          tree.makeVisible(new TreePath(treeNode.getPath()));

          if (parseNode.children() != null)
            for (int i=0; i<parseNode.children().size(); i++)
              {
                DefaultMutableTreeNode childTreeNode
                  = new DefaultMutableTreeNode(parseNode.child(i+1));
                treeModel.insertNodeInto(childTreeNode,treeNode,i);
                treeNodes.push(childTreeNode);
              }
        }
    }
}
