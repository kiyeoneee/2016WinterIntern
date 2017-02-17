package CategoryClassification;

//테이블 내용변경불가하게
//테이블 내용 수정할 때 상품명/카테고리를 선택하지 않은 경우 들에 대하여 경고창 띄워주기
import java.awt.EventQueue;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Component;

public class ClassificationFrame {
	JFrame frame;
	public static final int CATEGORY1 = 0, CATEGORY2 = 1, CATEGORY3 = 2, ITEM = 3;

	private JTable table;
	private JList itemNameList, keywordList;
	private JTextPane selectedItem, selectedKeywordCategory;
	private JButton addKeywordButton, deleteButton, saveEdidtedInfoButton, editButton, cancelButton, btnNewButton,
			keywordEditButton, keywordDeleteButton, keywordAddButton;
	private JSeparator partition;
	private DefaultTableModel keywordModel;
	private DefaultListModel keywordListModel = new DefaultListModel();

	private JList categoryListView1, categoryListView2, categoryListView3;

	private String[][][] categoryList, categoryName, categoryCode;
	private String[] itemList, category1Data, category2Data, category3Data, nullData = {};
	private Object nullObj[][] = {};
	private Object[][] tableModelObject;

	int selectedItemIndex;
	int categoryIdx1, categoryIdx2, categoryIdx3;
	String keywordSaveMode = null;

	boolean isListEditable = false;
	boolean isKeywordEditMode = false;
	boolean isItemFileExist = true;

	public HashMap<String, HashMap<String, String[]>> Item = new HashMap();
	public HashMap<String, String[]> getChild = new HashMap();
	public HashMap<String, HashMap<String, String[]>> keywordData = new HashMap();

	public ClassificationFrame() {
		try {
			Document documentCategory = getData("korea_category.xml");
			NodeList nodeCategory = documentCategory.getChildNodes();
			Document documentItem = getData("item(korean).xml");
			NodeList nodeItem = documentItem.getChildNodes();

			setItemArray(nodeItem.item(0).getChildNodes());
			setCategoryArray(nodeCategory.item(0).getChildNodes());

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("상품명 추천 카테고리 데이터가 없습니다.");
		}
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(10, 50, 1260, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		itemList = new String[Item.size()];
		Item.keySet().toArray(itemList);
		itemNameList = new JList(itemList);
		itemNameList.addListSelectionListener(new itemListSelectionListener());
		itemNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frame.getContentPane().add(new JScrollPane(itemNameList)).setBounds(6, 6, 280, 666);

		keywordList = new JList(keywordListModel);
		keywordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frame.getContentPane().add(new JScrollPane(keywordList)).setBounds(952, 6, 302, 634);

		keywordEditButton = new JButton("Edit Keyword");
		keywordEditButton.addActionListener(new keywordListActionListener());
		keywordEditButton.setBounds(945, 643, 106, 30);
		frame.getContentPane().add(keywordEditButton);

		keywordAddButton = new JButton("add Keyword");
		keywordAddButton.addActionListener(new keywordListActionListener());
		keywordAddButton.setBounds(1040, 643, 106, 30);
		frame.getContentPane().add(keywordAddButton);

		keywordDeleteButton = new JButton("Delete Keyword");
		keywordDeleteButton.addActionListener(new keywordListActionListener());
		keywordDeleteButton.setBounds(1135, 643, 126, 30);
		frame.getContentPane().add(keywordDeleteButton);

		selectedItem = new JTextPane();
		selectedItem.setEditable(false);
		frame.getContentPane().add(new JScrollPane(selectedItem)).setBounds(292, 6, 654, 55);

		keywordModel = new DefaultTableModel(nullObj, new Object[] { "Keyword", "Category1", "Category2", "Category3", "Code" });
		keywordModel.setRowCount(0);
		table = new JTable(keywordModel) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					categoryListView1.clearSelection();
					categoryListView2.setListData(nullData);
					categoryListView3.setListData(nullData);
					selectedKeywordCategory.setText("");
					isListEditable = false;
				}
			}
		});
		frame.getContentPane().add(new JScrollPane(table)).setBounds(292, 67, 593, 220);

		btnNewButton = new JButton(">>");
		btnNewButton.addActionListener(new saveKeywordActionListener());
		btnNewButton.setBounds(889, 145, 59, 60);
		frame.getContentPane().add(btnNewButton);

		editButton = new JButton("Edit");
		editButton.addActionListener(new editKeywordActionListener());
		editButton.setBounds(287, 290, 220, 30);
		frame.getContentPane().add(editButton);

		addKeywordButton = new JButton("Add");
		addKeywordButton.addActionListener(new editKeywordActionListener());
		addKeywordButton.setBounds(509, 290, 220, 30);
		frame.getContentPane().add(addKeywordButton);

		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new editKeywordActionListener());
		deleteButton.setBounds(731, 290, 220, 30);
		frame.getContentPane().add(deleteButton);

		partition = new JSeparator();
		partition.setBounds(290, 318, 658, 14);
		frame.getContentPane().add(partition);

		selectedKeywordCategory = new JTextPane();
		selectedKeywordCategory.setEditable(false);
		frame.getContentPane().add(new JScrollPane(selectedKeywordCategory)).setBounds(292, 334, 654, 50);

		categoryListView1 = new JList();
		categoryListView1.setListData(getStringForList1());
		categoryListView1.addListSelectionListener(new category1SelectionListener());
		categoryListView1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frame.getContentPane().add(new JScrollPane(categoryListView1)).setBounds(292, 390, 214, 250);

		categoryListView2 = new JList();
		categoryListView2.addListSelectionListener(new category2SelectionListener());
		categoryListView2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frame.getContentPane().add(new JScrollPane(categoryListView2)).setBounds(512, 390, 214, 250);

		categoryListView3 = new JList();
		categoryListView3.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frame.getContentPane().add(new JScrollPane(categoryListView3)).setBounds(732, 390, 214, 250);

		saveEdidtedInfoButton = new JButton("Save Edited Information");
		saveEdidtedInfoButton.addActionListener(new editCategoryActionListener());
		saveEdidtedInfoButton.setBounds(287, 643, 335, 30);
		frame.getContentPane().add(saveEdidtedInfoButton);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new editCategoryActionListener());
		cancelButton.setBounds(616, 643, 335, 30);
		frame.getContentPane().add(cancelButton);

	}

	private class keywordListActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			if (button.getText().equals("Edit Keyword")) {
				if (keywordList.isSelectionEmpty()) {
					JOptionPane.showMessageDialog(null, "Please select the keyword to edit.");
					return;
				}
				categoryListView1.clearSelection(); categoryListView2.setListData(nullData); categoryListView3.setListData(nullData);
				isKeywordEditMode = true;
				keywordSaveMode = "edit";
				isListEditable = true;
				itemNameList.clearSelection();
				selectedItem.setText("");
				keywordModel.setRowCount(0);

				String listValue = keywordList.getSelectedValue().toString();
				String[] keyword = listValue.split("\t\\( ");
				// keyword[0] => keyword

				String[] getCode = keyword[1].split(" : ");
				String[] keywordCategoryCode = new String[3];
				keywordCategoryCode[CATEGORY1] = getCode[0].substring(0, 3);
				keywordCategoryCode[CATEGORY2] = getCode[0].substring(3, 6);
				if (getCode[0].length() > 6)
					keywordCategoryCode[CATEGORY3] = getCode[0].substring(6, 9);

				String keyW = "Selected Keyword : " + keyword[0] + "\n\nCategory before editing : ";
				int[] codeIndex = getCodeToIndex(getCode[0]);
				keyW += categoryName[codeIndex[CATEGORY1]][0][0]
						+ categoryName[codeIndex[CATEGORY1]][codeIndex[CATEGORY2]][0];
				if (getCode[0].length() > 6)
					keyW += categoryName[codeIndex[CATEGORY1]][codeIndex[CATEGORY2]][codeIndex[CATEGORY3]];

				selectedKeywordCategory.setText(keyW);
			} else if (button.getText().equals("add Keyword")) {
				String kw = JOptionPane.showInputDialog("Please enter a keyword to add.");
				if (kw == null)
					return;
				categoryListView1.clearSelection(); categoryListView2.setListData(nullData); categoryListView3.setListData(nullData);
				isKeywordEditMode = true;
				keywordSaveMode = "add";
				isListEditable = true;
				itemNameList.clearSelection();
				selectedItem.setText("");
				keywordModel.setRowCount(0);

				selectedKeywordCategory.setText(kw);
			} else if (button.getText().equals("Delete Keyword")) {
				selectedItem.setText("");
				categoryListView1.clearSelection(); categoryListView2.setListData(nullData); categoryListView3.setListData(nullData);
				String listValue = keywordList.getSelectedValue().toString();
				String[] keyword = listValue.split("\t\\( ");
				// keyword[0] => keyword

				String[] getCode = keyword[1].split(" : ");
				HashMap<String, String[]> getKeywordData = keywordData.get(keyword[0]);
				getKeywordData.remove(getCode[0]);
				keywordData.put(keyword[0], getKeywordData);

				updateKeywordModel();
			}
		}
	}

	private class itemListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent itemListSelectionEvent) {
			boolean adjust = itemListSelectionEvent.getValueIsAdjusting();
			if (!adjust) {
				JList list = (JList) itemListSelectionEvent.getSource();
				selectedItemIndex = list.getSelectedIndex();
				if (selectedItemIndex >= 0) {
					String itemName = itemList[selectedItemIndex];
					getChild = Item.get(itemName);
					String itemKeyWord = getChild.keySet().toString();
					selectedItem.setText(itemName.concat("\n\nKeyword => ").concat(itemKeyWord));

					getTableModel(getChild);
				}
			}
		}
	}

	private class category1SelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (isListEditable == false) {
				categoryListView1.clearSelection();
				return;
			}

			if (categoryListView1.getSelectedIndex() == -1)
				return;
			boolean adjust = e.getValueIsAdjusting();
			if (!adjust) {
				JList l = (JList) e.getSource();
				categoryListView2.setListData(nullData);
				categoryListView3.setListData(nullData);

				categoryIdx1 = l.getSelectedIndex();
				String[] category2Data = new String[categoryName[categoryIdx1].length];

				for (int i = 1; i < categoryName[categoryIdx1].length; i++)
					category2Data[i] = categoryName[categoryIdx1][i][0] + "\t( " + categoryCode[categoryIdx1][i][0]
							+ " )";

				categoryListView2.setListData(category2Data);
			}
		}
	}

	private class category2SelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (categoryListView2.getSelectedIndex() == -1)
				return;
			boolean adjust = e.getValueIsAdjusting();
			if (!adjust) {
				JList l = (JList) e.getSource();
				categoryListView3.setListData(nullData);

				categoryIdx2 = l.getSelectedIndex();
				String[] category3Data = new String[categoryName[categoryIdx1][categoryIdx2].length];

				for (int i = 1; i < categoryName[categoryIdx1][categoryIdx2].length; i++)
					category3Data[i] = categoryName[categoryIdx1][categoryIdx2][i] + "\t( "
							+ categoryCode[categoryIdx1][categoryIdx2][i] + " )";

				categoryListView3.setListData(category3Data);

			}
		}
	}

	private class category3SelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (categoryListView2.getSelectedIndex() == -1)
				return;
			boolean adjust = e.getValueIsAdjusting();
			if (!adjust) {

			}
		}
	}

	private class editKeywordActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (itemNameList.isSelectionEmpty() == true) {
				JOptionPane.showMessageDialog(null, "Please select a product name to edit the category.");
				return;
			}

			JButton b = (JButton) e.getSource();
			if (b.getText().equals("Add")) {
				String keyW[] = new String[5];
				keyW[4] = "0";
				String keyw = JOptionPane.showInputDialog("Please enter a keyword to add.");
				if (keyw.length() == 0) {
					JOptionPane.showMessageDialog(null, "You have entered a mistake.");
					return;
				}
				keyW[0] = keyw;
				keywordModel.insertRow(0, keyW);
				
			} else if (b.getText().equals("Delete")) {
				if (table.getSelectedRow() == -1) {
					JOptionPane.showMessageDialog(null, "Please select keyword to delete.");
					return;
				}
				int index = table.getSelectedRow();
				keywordModel.removeRow(index);
			} else if (b.getText().equals("Edit")) {
				if (table.getSelectedRow() == -1) {
					JOptionPane.showMessageDialog(null, "Please select keyword to edit.");
					return;
				}
				isListEditable = true;
				showSelectedKeywordInfo(table.getValueAt(table.getSelectedRow(), 0).toString(),
						table.getValueAt(table.getSelectedRow(), 4).toString());
			}
		}
	}

	private void showSelectedKeywordInfo(String keyW, String code) {
		int[] indexAry = getCodeToIndex(code);

		keyW = "Selected Keyword : " + keyW + "\n\nCategory before editing : ";

		if (indexAry[0] != -1)
			keyW += (categoryName[indexAry[0]][0][0]);
		if (indexAry[1] != -1)
			keyW += (" -> " + categoryName[indexAry[0]][indexAry[1]][0]);
		if (indexAry[2] != -1)
			keyW += (" -> " + categoryName[indexAry[0]][indexAry[1]][indexAry[2]]);

		selectedKeywordCategory.setText(keyW);
	}

	private class editCategoryActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (isKeywordEditMode) {
				if (!isCategoryCanSave()) {
					JOptionPane.showMessageDialog(null, "Please select all categories.");
					return;
				}

				if (keywordSaveMode == "edit") {
					String listValue = keywordList.getSelectedValue().toString();
					// keyword[0] => keyword
					String[] keyword = listValue.split("\t\\( ");
					String[] getCode = keyword[1].split(" : ");
					String code = "";
					HashMap<String, String[]> getKeywordData = keywordData.get(keyword[0]);
					String[] categoryEdit = new String[4];

					categoryEdit[CATEGORY1] = categoryName[categoryListView1.getSelectedIndex()][0][0];
					code += categoryCode[categoryListView1.getSelectedIndex()][0][0];
					categoryEdit[CATEGORY2] = categoryName[categoryListView1.getSelectedIndex()][categoryListView2
							.getSelectedIndex()][0];
					code += categoryCode[categoryListView1.getSelectedIndex()][categoryListView2.getSelectedIndex()][0];
					if (!categoryListView3.isSelectionEmpty()) {
						categoryEdit[CATEGORY3] = categoryName[categoryListView1.getSelectedIndex()][categoryListView2
								.getSelectedIndex()][categoryListView3.getSelectedIndex()];
						code += categoryCode[categoryListView1.getSelectedIndex()][categoryListView2
								.getSelectedIndex()][categoryListView3.getSelectedIndex()];
					} else
						categoryEdit[CATEGORY3] = null;
					categoryEdit[ITEM] = null;
					getKeywordData.remove(getCode[0]);
					getKeywordData.put(code, categoryEdit);
					keywordData.put(keyword[0], getKeywordData);

					selectedKeywordCategory.setText("");
					categoryListView1.clearSelection();
					categoryListView2.setListData(nullData);
					categoryListView3.setListData(nullData);

					updateKeywordModel();
				}

				else if (keywordSaveMode == "add") {
					String keyWordS = selectedKeywordCategory.getText().toString();
					HashMap<String, String[]> getKeywordData = new HashMap();
					String code = "";
					String[] categoryEdit = new String[4];

					categoryEdit[CATEGORY1] = categoryName[categoryListView1.getSelectedIndex()][0][0];
					code += categoryCode[categoryListView1.getSelectedIndex()][0][0];
					categoryEdit[CATEGORY2] = categoryName[categoryListView1.getSelectedIndex()][categoryListView2
							.getSelectedIndex()][0];
					code += categoryCode[categoryListView1.getSelectedIndex()][categoryListView2.getSelectedIndex()][0];
					if (!categoryListView3.isSelectionEmpty()) {
						categoryEdit[CATEGORY3] = categoryName[categoryListView1.getSelectedIndex()][categoryListView2
								.getSelectedIndex()][categoryListView3.getSelectedIndex()];
						code += categoryCode[categoryListView1.getSelectedIndex()][categoryListView2
								.getSelectedIndex()][categoryListView3.getSelectedIndex()];
					} else
						categoryEdit[CATEGORY3] = null;
					categoryEdit[ITEM] = null;

					getKeywordData.put(code, categoryEdit);
					keywordData.put(keyWordS, getKeywordData);

					selectedKeywordCategory.setText("");
					categoryListView1.clearSelection();
					categoryListView2.setListData(nullData);
					categoryListView3.setListData(nullData);

					updateKeywordModel();
				}

				keywordSaveMode = null;
				isListEditable = false;
				isKeywordEditMode = false;
				return;
			}

			if (itemNameList.isSelectionEmpty() & keywordList.isSelectionEmpty()) {
				JOptionPane.showMessageDialog(null, "Please select a product name to edit the category.");
				return;
			}

			JButton b = (JButton) e.getSource();
			if (b.getText().equals("Save Edited Information")) {
				if (table.getSelectedRow() == -1) {
					JOptionPane.showMessageDialog(null, "Please select keyword.");
					return;
				}
				if (isListEditable == false) {
					JOptionPane.showMessageDialog(null, "Select the keyword and press the edit button.");
					return;
				}

				if (isCategoryCanSave())
					updateEditedCategory();
				else {
					JOptionPane.showMessageDialog(null, "Please select all categories.");
					return;
				}

			} else if (b.getText().equals("Cancel")) {
				if (table.getSelectedRow() == -1) {
					JOptionPane.showMessageDialog(null, "Please select keyword.");
					return;
				}

				table.clearSelection();
			}
		}
	}

	public boolean isCategoryCanSave() {
		if (!categoryListView1.isSelectionEmpty() & !categoryListView2.isSelectionEmpty()
				& !categoryListView3.isSelectionEmpty())
			return true;
		else if (categoryName[categoryListView1.getSelectedIndex()][categoryListView2.getSelectedIndex()].length == 1
				& !categoryListView1.isSelectionEmpty() & !categoryListView2.isSelectionEmpty())
			return true;
		else
			return false;
	}

	private class saveKeywordActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (itemNameList.isSelectionEmpty())
				return;
			int row = keywordModel.getRowCount(), column = keywordModel.getColumnCount();
			String SelectedItem = itemNameList.getSelectedValue().toString();
			HashMap<String, String[]> forChangeData = new HashMap();

			for (int i = 0; i < row; i++) {
				if (keywordModel.getValueAt(i, 4).toString().length() < 9) {
					JOptionPane.showMessageDialog(null, "Please set all categories.");
					return;
				}

				String tableValue = keywordModel.getValueAt(i, 0).toString();
				boolean isExist = false;
				String[] code;
				if (forChangeData.containsKey(tableValue) == true) {
					String[] toCheckSaved = forChangeData.get(tableValue);
					for (int j = 0; j < toCheckSaved.length; j++) {
						if (toCheckSaved[j] == keywordModel.getValueAt(i, 4)) {
							isExist = true;
							break;
						}
					}
					if (isExist == false) {
						code = new String[toCheckSaved.length + 1];
						for (int j = 0; j < code.length - 1; j++)
							code[j] = toCheckSaved[j];
						code[code.length - 1] = keywordModel.getValueAt(i, 4).toString();
						forChangeData.put(tableValue, code);
					} else
						forChangeData.put(tableValue, toCheckSaved);
				} else {
					code = new String[1];
					code[0] = keywordModel.getValueAt(i, 4).toString();
					forChangeData.put(tableValue, code);
				}
			}
			Item.put(SelectedItem, forChangeData);

			String keyword = null;
			for (int i = 0; i < row; i++) {
				String[] keywordCategory = new String[4];
				keyword = keywordModel.getValueAt(i, 0).toString();
				String keywordCode = keywordModel.getValueAt(i, 4).toString();

				HashMap<String, String[]> checkKeyword = new HashMap();
				if (keywordData.containsKey(keyword) == true) {
					checkKeyword = keywordData.get(keyword);

					// 해당하는 keyword에 동일한 categoryCode가 존재하는지를 확인
					if (checkKeyword != null && checkKeyword.containsKey(checkKeyword) == true)
						continue;

					// 존재하지 않는 경우
					for (int j = 1; j < column - 1; j++) {
						if (keywordModel.getValueAt(i, j) == null)
							continue;
						String s = keywordModel.getValueAt(i, j).toString();
						keywordCategory[j - 1] = s;
					}
					keywordCategory[3] = SelectedItem;

					checkKeyword.put(keywordCode, keywordCategory);
					keywordData.put(keyword, checkKeyword);
				}

				else {
					HashMap<String, String[]> addCode = new HashMap();

					for (int j = 1; j < column - 1; j++) {
						if (keywordModel.getValueAt(i, j) == null) {
							keywordCategory[j - 1] = null;
							continue;
						}
						String s = keywordModel.getValueAt(i, j).toString();
						keywordCategory[j - 1] = s;
					}
					keywordCategory[3] = SelectedItem;
					addCode.put(keywordCode, keywordCategory);
					keywordData.put(keyword, addCode);
				}
				updateKeywordModel();
			}
			// int selectedIdx = itemNameList.getSelectedIndex();
			// itemNameList.getComponent(selectedIdx).setBackground(Color.YELLOW);

			itemNameList.clearSelection();
			selectedItem.setText("");
			keywordModel.setRowCount(0);
		}
	}

	public void updateKeywordModel() {
		String[] keyWordSet = new String[keywordData.keySet().size()];
		keywordData.keySet().toArray(keyWordSet);

		int size = 0;
		for (int i = 0; i < keyWordSet.length; i++) {
			HashMap<String, String[]> getCodeSet = keywordData.get(keyWordSet[i]);
			size += getCodeSet.keySet().size();
		}

		String[] keywordListData = new String[size];
		for (int i = 0, idx = 0; i < keyWordSet.length; i++) {
			HashMap<String, String[]> getCodeSet = keywordData.get(keyWordSet[i]);
			String[] codeSet = new String[getCodeSet.keySet().size()];
			String[] getCategory = new String[4];
			getCodeSet.keySet().toArray(codeSet);

			for (int j = 0; j < codeSet.length; j++) {
				getCategory = getCodeSet.get(codeSet[j]);
				String category = codeSet[j] + " : " + getCategory[CATEGORY1].toString() + ", "
						+ getCategory[CATEGORY2].toString();
				if (getCategory[CATEGORY3] != null)
					category += ", " + getCategory[CATEGORY3].toString();
				keywordListData[idx++] = keyWordSet[i] + "\t( " + category + " )";
			}
		}

		keywordList.setListData(keywordListData);
		hashmapToXml("keyword.xml");
	}

	public void updateEditedCategory() {
		int[] idx = { categoryListView1.getSelectedIndex(), categoryListView2.getSelectedIndex(),
				categoryListView3.getSelectedIndex() };
		Object[] updateData = new Object[5];
		String getCode = "";

		DefaultTableModel tm = (DefaultTableModel) table.getModel();
		int tableRow = table.getSelectedRow();

		for (int i = 0; i < 4; i++) {
			if (table.getValueAt(tableRow, i) == null)
				continue;
			updateData[i] = table.getValueAt(tableRow, i).toString();
		}

		updateData[1] = categoryName[idx[0]][0][0].toString();
		getCode += categoryCode[idx[0]][0][0].toString();
		updateData[2] = categoryName[idx[0]][idx[1]][0].toString();
		getCode += categoryCode[idx[0]][idx[1]][0].toString();
		if (categoryName[categoryListView1.getSelectedIndex()][categoryListView2.getSelectedIndex()].length != 1) {
			updateData[3] = categoryName[idx[0]][idx[1]][idx[2]].toString();
			getCode += categoryCode[idx[0]][idx[1]][idx[2]].toString();
		}
		updateData[4] = getCode;

		tm.removeRow(tableRow);
		tm.insertRow(0, updateData);
	}

	public void getTableModel(HashMap<String, String[]> tableMap) {
		String[] getKey = new String[tableMap.size()];
		int num = 0;

		tableMap.keySet().toArray(getKey);

		for (int i = 0; i < getKey.length; i++) {
			num++;
			if (tableMap.get(getKey[i]) == null) {
				continue;
			}
			num += (tableMap.get(getKey[i]).length - 1);
		}

		tableModelObject = new Object[num][5];
		for (int i = 0, k = 0; i < getKey.length; i++) {
			tableModelObject[k][0] = getKey[i];
			if (tableMap.get(getKey[i]) == null) {
				tableModelObject[k][4] = 0;
				k++;
				continue;
			}
			for (String j : tableMap.get(getKey[i])) {
				tableModelObject[k][0] = getKey[i];
				int[] indexAry = getCodeToIndex(j);

				if (indexAry[0] != -1)
					tableModelObject[k][1] = categoryName[indexAry[0]][0][0];
				if (indexAry[1] != -1)
					tableModelObject[k][2] = categoryName[indexAry[0]][indexAry[1]][0];
				if (indexAry[2] != -1)
					tableModelObject[k][3] = categoryName[indexAry[0]][indexAry[1]][indexAry[2]];

				tableModelObject[k][4] = j;
				k++;
			}
		}

		keywordModel = new DefaultTableModel(tableModelObject,
				new Object[] { "Keyword", "Category1", "Category2", "Category3", "Code" });
		table.setModel(keywordModel);
	}

	public int[] getCodeToIndex(String codeStr) {
		int[] category = { -1, -1, -1 };
		String[] keywordCategory = getCategoryCode(codeStr);

		for (int check = 0; check < 3 && keywordCategory[check] != null; check++) {
			if (check == 0) {
				for (int idx = 0; idx < categoryCode.length; idx++) {
					if (categoryCode[idx][0][0].equals(keywordCategory[check]) == false)
						continue;
					category[0] = idx;
					break;
				}
			}

			else if (check == 1) {
				for (int idx = 1; idx < categoryCode[category[0]].length; idx++) {
					if (categoryCode[category[0]][idx][0].equals(keywordCategory[check]) == false)
						continue;
					category[1] = idx;
					break;
				}
			}

			else if (check == 2) {
				for (int idx = 1; idx < categoryCode[category[0]][category[1]].length; idx++) {
					if (categoryCode[category[0]][category[1]][idx].equals(keywordCategory[check]) == false)
						continue;
					category[2] = idx;
					break;
				}
			}
		}
		return category;
	}

	public String[] getCategoryCode(String entireCode) {
		String[] code = new String[3];
		int codeLength = entireCode.length() / 3, i = 0;

		for (; i < codeLength; i++)
			code[i] = entireCode.substring(i * 3, i * 3 + 3);

		for (; i < 3; i++)
			code[i] = null;

		return code;
	}

	public String[] getStringForList1() {
		String[] makeListData = new String[categoryName.length];

		for (int i = 0; i < categoryName.length; i++)
			makeListData[i] = categoryName[i][0][0].toString() + "\t( " + categoryCode[i][0][0] + " )";

		return makeListData;
	}

	public static Document getData(String path) {
		try {
			File xmlFile = new File(path);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			factory.setIgnoringElementContentWhitespace(true);
			Document document = builder.parse(xmlFile);
			document.getDocumentElement().normalize();

			return document;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setItemArray(NodeList node) {
		if (node.getLength() == 1 && node.item(0).getNodeName() == "#text")
			return;

		for (int i = 0; i < node.getLength(); i++) {
			if (node.item(i) instanceof Element == false)
				continue;

			HashMap<String, String[]> valueData = new HashMap();
			NodeList keyWordNodes = node.item(i).getChildNodes();
			for (int j = 0; j < keyWordNodes.getLength(); j++) {
				NodeList recommandCategoryCode = keyWordNodes.item(j).getChildNodes();
				String[] categoryCode = new String[recommandCategoryCode.getLength()];

				for (int k = 0; k < recommandCategoryCode.getLength(); k++) {
					Element elemC = (Element) recommandCategoryCode.item(k);
					Node n = elemC.getFirstChild();
					categoryCode[k] = n.getNodeValue();
				}

				Element elemN = (Element) keyWordNodes.item(j);
				if (recommandCategoryCode.getLength() != 0)
					valueData.put(elemN.getAttribute("name").trim(), categoryCode);
				else
					valueData.put(elemN.getAttribute("name").trim(), null);
			}

			Element elemH = (Element) node.item(i);
			Item.put(elemH.getAttribute("name").trim(), valueData);
		}
	}

	public void setCategoryArray(NodeList node) {
		categoryList = new String[node.getLength() / 2][][];
		categoryName = new String[node.getLength() / 2][][];
		categoryCode = new String[node.getLength() / 2][][];

		for (int i = 0; i < node.getLength(); i++) {
			if (node.item(i).getNodeName() == "#text")
				continue;
			NodeList depth1 = node.item(i).getChildNodes();

			int indexI = i / 2;
			categoryList[indexI] = new String[depth1.getLength() / 2 + 1][];
			categoryList[indexI][0] = new String[1];
			categoryName[indexI] = new String[depth1.getLength() / 2 + 1][];
			categoryName[indexI][0] = new String[1];
			categoryCode[indexI] = new String[depth1.getLength() / 2 + 1][];
			categoryCode[indexI][0] = new String[1];

			for (int j = 1; j < depth1.getLength(); j++) {
				if (depth1.item(j).getNodeName() == "#text")
					continue;
				int indexJ = j / 2;
				NodeList depth2 = depth1.item(j).getChildNodes();

				categoryList[indexI][indexJ + 1] = new String[depth2.getLength() / 2 + 1];
				categoryName[indexI][indexJ + 1] = new String[depth2.getLength() / 2 + 1];
				categoryCode[indexI][indexJ + 1] = new String[depth2.getLength() / 2 + 1];

				for (int k = 1; k < depth2.getLength(); k++) {
					if (depth2.item(k).getNodeName() == "#text")
						continue;
					int indexK = k / 2;

					// 가장 하단 카테고리
					if (depth2.item(k).getNodeType() == Node.ELEMENT_NODE) {
						Element elemH2 = (Element) depth2.item(k);
						String str = elemH2.getAttribute("name");
						categoryList[indexI][indexJ + 1][indexK + 1] = str;
						categoryName[indexI][indexJ + 1][indexK + 1] = str.substring(4);
						categoryCode[indexI][indexJ + 1][indexK + 1] = str.substring(0, 3);
					}
				}

				// 중간 카테고리
				if (depth1.item(j).getNodeType() == Node.ELEMENT_NODE) {
					Element elemH1 = (Element) depth1.item(j);
					String str = elemH1.getAttribute("name");
					categoryList[indexI][indexJ + 1][0] = str;
					categoryName[indexI][indexJ + 1][0] = str.substring(4);
					categoryCode[indexI][indexJ + 1][0] = str.substring(0, 3);
				}
			}
			// 상단 카테고리
			if (node.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element elemH = (Element) node.item(i);
				String str = elemH.getAttribute("name");
				categoryList[indexI][0][0] = str;
				categoryName[indexI][0][0] = str.substring(4);
				categoryCode[indexI][0][0] = str.substring(0, 3);
			}
		}
	}

	public void hashmapToXml(String path) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(path),
					Charset.forName("UTF-8"));
			PrintWriter printKeyword = new PrintWriter(outputStreamWriter);

			String keywordString = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root>";

			String[] firstDepth = new String[keywordData.size()];
			keywordData.keySet().toArray(firstDepth);

			for (int i = 0; i < keywordData.size(); i++) {
				keywordString += "<keyword name=\"" + firstDepth[i] + "\">";

				HashMap<String, String[]> childMap = keywordData.get(firstDepth[i]);
				String[] secondDepth = new String[childMap.size()];
				childMap.keySet().toArray(secondDepth);
				for (int j = 0; j < secondDepth.length; j++) {
					keywordString += "<category code=\"" + secondDepth[j] + "\">";

					String[] thirdDepth = childMap.get(secondDepth[j]);

					keywordString += "<key firstCategory=\"" + thirdDepth[CATEGORY1] + "\" secondCategory=\""
							+ thirdDepth[CATEGORY2] + "\" thirdCategory=\"" + thirdDepth[CATEGORY3] + "\" item=\""
							+ thirdDepth[ITEM] + "\"></key>";
					keywordString += "</category>";
				}

				keywordString += "</keyword>";
			}

			keywordString += "</root>";

			printKeyword.write(keywordString.toString());

			printKeyword.close();
		} catch (IOException e) {
			System.out.println("File Not Found\n");
			e.printStackTrace();
		}
	}
}