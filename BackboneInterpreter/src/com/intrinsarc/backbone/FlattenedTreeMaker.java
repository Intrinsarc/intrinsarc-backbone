/*******************************************************************************
 * Copyright 2005-2010, Andrew McVeigh.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.intrinsarc.backbone;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import com.intrinsarc.backbone.nodes.simple.*;
import com.intrinsarc.deltaengine.base.*;

/**
 * make a tree showing the flattened structure of the Backbone program
 * @author andrew
 */
public class FlattenedTreeMaker
{
	public FlattenedTreeMaker()
	{
	}

	public void makeTreeWindow(DEStratum perspective, DEComponent flat)
	{
		JFrame frame = new JFrame("Flattened");
		frame.setLayout(new BorderLayout());

		frame.setPreferredSize(new Dimension(300, 400));
		frame.setLocation(200, 200);
		
		frame.add(makeTree(perspective, flat), BorderLayout.CENTER);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	private JPanel makeTree(DEStratum perspective, DEComponent flat)
	{
		JPanel pane = new JPanel(new BorderLayout());
		pane.setBackground(Color.GREEN);
		BBSimpleElementRegistry registry = new BBSimpleElementRegistry(perspective, flat);
		BBSimpleComponent top = new BBSimpleComponent(registry, flat);
		top.flatten(registry);
		
		JTree tree = makeTree(top);
		tree.setRootVisible(false);
		JScrollPane scroller = new JScrollPane(tree);
		pane.add(scroller, BorderLayout.CENTER);

		return pane;
	}

	private JTree makeTree(BBSimpleComponent top)
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		JTree tree = new JTree(root);		
		addNodes(root, top, 0);
		expandTree(tree, null, true, 4);
		return tree;
	}

	private void addNodes(DefaultMutableTreeNode parent, BBSimpleObject object, int depth)
	{
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(object.getTreeDescription());
		parent.add(node);
		Map<String, java.util.List<? extends BBSimpleObject>> map = object.getChildren(depth == 0);
		if (map != null)
			for (String head : map.keySet())
			{
				java.util.List<? extends BBSimpleObject> ls = map.get(head);
				if (!isReallyEmpty(ls))
				{
					DefaultMutableTreeNode header = new DefaultMutableTreeNode(head);
					node.add(header);
					
					for (BBSimpleObject obj : ls)
						if (obj != null)
							addNodes(header, obj, depth + 1);
				}
			}
	}
	
	private boolean isReallyEmpty(java.util.List<? extends BBSimpleObject> ls)
	{
		if (ls == null)
			return true;
		for (BBSimpleObject obj : ls)
			if (obj != null)
				return false;
		return true;
	}
	
  private static void expandTree(JTree tree, TreePath parent, boolean expand, int depth)
  {
  	if (depth == 0)
  		return;
  	if (parent == null)
  		parent = new TreePath(tree.getModel().getRoot());
  	
    // Traverse children
    TreeNode node = (TreeNode) parent.getLastPathComponent();
    if (node.getChildCount() >= 0)
    {
      for (Enumeration e = node.children(); e.hasMoreElements();)
      {
        TreeNode n = (TreeNode) e.nextElement();
        TreePath path = parent.pathByAddingChild(n);
        
        expandTree(tree, path, expand, depth - 1);
      }
    }

    // Expansion or collapse must be done bottom-up
    if (!parent.getLastPathComponent().equals(tree.getModel().getRoot()) || expand)
    {
      if (expand)
        tree.expandPath(parent);
      else
        tree.collapsePath(parent);
    }
  }
}

