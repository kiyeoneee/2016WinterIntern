package urlClassificaition;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ClassificationFrame {
	JFrame frame;
	int count = 0;
	public static final int CATEGORY1 = 0, CATEGORY2 = 1, CATEGORY3 = 2, TITLE = 3;

	private JTable table;
	private JList urlShowList, urlCategoryList;
	private JTextPane selectedUrl, selectedKeywordCategory;
	private JButton addKeywordButton, deleteButton, saveEdidtedInfoButton, editButton, cancelButton, btnNewButton,
			keywordEditButton, keywordDeleteButton, keywordAddButton;
	private JSeparator partition;
	private DefaultTableModel urlCategoryModel;
	private DefaultListModel keywordListModel = new DefaultListModel();

	private JList categoryListView1, categoryListView2, categoryListView3;

	private String[][][] categoryList, categoryName, categoryCode;
	private String[] urlList, category1Data, category2Data, category3Data, nullData = {};
	private Object nullObj[][] = {};
	private Object[][] tableModelObject;

	int selectedUrlIndex;
	int categoryIdx1, categoryIdx2, categoryIdx3;
	String keywordSaveMode = null;

	boolean isListEditable = false;
	boolean isKeywordEditMode = false;
	boolean isItemFileExist = true;

	public HashMap<String, String> Url = new HashMap();
	public HashMap<String, HashMap<String, String[]>> urlCategoryData = new HashMap();

	public ClassificationFrame() {
		try {
			Document documentCategory = getData("japan_category.xml");
			NodeList nodeCategory = documentCategory.getChildNodes();
			Document documentItem = getData("urlData.xml");
			NodeList nodeItem = documentItem.getChildNodes();

			setUrlArray(nodeItem.item(0).getChildNodes());
			setCategoryArray(nodeCategory.item(0).getChildNodes());

		} catch (Exception e) {
			e.printStackTrace();
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

		urlList = new String[Url.size()];
		Url.keySet().toArray(urlList);
		urlShowList = new JList(urlList);
		urlShowList.addListSelectionListener(new urlListSelectionListener());
		urlShowList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frame.getContentPane().add(new JScrollPane(urlShowList)).setBounds(6, 6, 280, 666);

		keywordList = new JList(keywordListModel);
		keywordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frame.getContentPane().add(new JScrollPane(keywordList)).setBounds(952, 6, 302, 634);

		keywordEditButton = new JButton("Edit title");
		// keywordEditButton.addActionListener(new keywordListActionListener());
		keywordEditButton.setBounds(945, 643, 106, 30);
		frame.getContentPane().add(keywordEditButton);

		keywordAddButton = new JButton("add title");
		// keywordAddButton.addActionListener(new keywordListActionListener());
		keywordAddButton.setBounds(1040, 643, 106, 30);
		frame.getContentPane().add(keywordAddButton);

		keywordDeleteButton = new JButton("Delete title");
		// keywordDeleteButton.addActionListener(new
		// keywordListActionListener());
		keywordDeleteButton.setBounds(1135, 643, 126, 30);
		frame.getContentPane().add(keywordDeleteButton);

		selectedUrl = new JTextPane();
		selectedUrl.setEditable(false);
		frame.getContentPane().add(new JScrollPane(selectedUrl)).setBounds(292, 6, 654, 55);

		urlCategoryModel = new DefaultTableModel(nullObj,
				new Object[] { "Category1", "Category2", "Category3", "Code" });
		urlCategoryModel.setRowCount(0);
		table = new JTable(urlCategoryModel) {
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
		// btnNewButton.addActionListener(new saveKeywordActionListener());
		btnNewButton.setBounds(889, 145, 59, 60);
		frame.getContentPane().add(btnNewButton);

		editButton = new JButton("Edit");
		editButton.addActionListener(new editCategoryActionListener());
		editButton.setBounds(287, 290, 220, 30);
		frame.getContentPane().add(editButton);

		addKeywordButton = new JButton("Add");
		addKeywordButton.addActionListener(new editCategoryActionListener());
		addKeywordButton.setBounds(509, 290, 220, 30);
		frame.getContentPane().add(addKeywordButton);

		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new editCategoryActionListener());
		deleteButton.setBounds(731, 290, 220, 30);
		frame.getContentPane().add(deleteButton);

		partition = new JSeparator();
		partition.setBounds(290, 318, 658, 14);
		frame.getContentPane().add(partition);

		selectedKeywordCategory = new JTextPane();
		selectedKeywordCategory.setEditable(false);
		frame.getContentPane().add(new JScrollPane(selectedKeywordCategory)).setBounds(292, 334, 654, 50);

		categoryListView1 = new JList();
		// categoryListView1.setListData(getStringForList1());
		// categoryListView1.addListSelectionListener(new
		// category1SelectionListener());
		categoryListView1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frame.getContentPane().add(new JScrollPane(categoryListView1)).setBounds(292, 390, 214, 250);

		categoryListView2 = new JList();
		// categoryListView2.addListSelectionListener(new
		// category2SelectionListener());
		categoryListView2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frame.getContentPane().add(new JScrollPane(categoryListView2)).setBounds(512, 390, 214, 250);

		categoryListView3 = new JList();
		categoryListView3.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frame.getContentPane().add(new JScrollPane(categoryListView3)).setBounds(732, 390, 214, 250);

		saveEdidtedInfoButton = new JButton("Save Edited Information");
		// saveEdidtedInfoButton.addActionListener(new
		// editCategoryActionListener());
		saveEdidtedInfoButton.setBounds(287, 643, 335, 30);
		frame.getContentPane().add(saveEdidtedInfoButton);

		cancelButton = new JButton("Cancel");
		// cancelButton.addActionListener(new editCategoryActionListener());
		cancelButton.setBounds(616, 643, 335, 30);
		frame.getContentPane().add(cancelButton);

	}
	
	private class editCategoryActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (urlShowList.isSelectionEmpty() == true) {
				JOptionPane.showMessageDialog(null, "Please select a product name to edit the category.");
				return;
			}

			JButton b = (JButton) e.getSource();
			if (b.getText().equals("Add")) {
				String[] keyW = new String[5];
				keyW[0] = JOptionPane.showInputDialog("Please enter a keyword to add.");
				if (keyW[0] != null)
					urlCategoryModel.insertRow(0, keyW);
			} else if (b.getText().equals("Delete")) {
				if (table.getSelectedRow() == -1) {
					JOptionPane.showMessageDialog(null, "Please select keyword to delete.");
					return;
				}
				int index = table.getSelectedRow();
				urlCategoryModel.removeRow(index);
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

	private class urlListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent itemListSelectionEvent) {
			boolean adjust = itemListSelectionEvent.getValueIsAdjusting();
			if (!adjust) {
				JList list = (JList) itemListSelectionEvent.getSource();
				selectedUrlIndex = list.getSelectedIndex();
				if (selectedUrlIndex >= 0) {
					String getUrl = urlList[selectedUrlIndex];
					String getUrlTitle = Url.get(getUrl).toString();
					selectedUrl.setText("URL : " + getUrl + "\n\n TITLE : " + getUrlTitle);

					urlCategoryModel.setRowCount(0);
				}
			}
		}
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

	public void setUrlArray(NodeList node) {
		if (node.getLength() == 1 && node.item(0).getNodeName() == "#text")
			return;

		for (int i = 0; i < node.getLength(); i++) {
			if (node.item(i) instanceof Element == false)
				continue;

			String urlTitle;
			if (!node.item(i).hasChildNodes())
				urlTitle = null;
			else {
				NodeList urlNodes = node.item(i).getChildNodes();
				urlTitle = urlNodes.item(0).getNodeValue();
			}
			
			Element elemH = (Element) node.item(i);
			if (urlTitle == null)
				Url.put(elemH.getAttribute("name").trim(), urlTitle);
			else
				Url.put(elemH.getAttribute("name").trim(), urlTitle.trim());
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

}