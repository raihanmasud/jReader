package uo.cis.editor;

import static java.lang.System.out;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;




public class Editor extends JFrame implements ActionListener, AdjustmentListener, Runnable{

	private String filePath = "";
	private static final long serialVersionUID = 1L;
	Container mainEditorPane = null;
	JScrollPane scrollPane=null;
	JEditorPane editorPane = null;
	JEditorPane rightNotePane = null;
	JPanel leftPanel = null;
	JPanel bottomPanel = null;
	boolean isSetHighlight = false;
	Highlighter htr;
	JButton highLightButton;
	JButton printHighlightedButton;
	static final int mainWindowHeight = 700;
	static final int mainWindowWidth = 1000;
	LinkedList<Highlight> highlightedTexts ;
	boolean isSetAddNote = false;
	JTextField pageNumberBox;
	static int scrollValue;
	
	class Highlight{
		
		int start;
		int end;
		String text;
		
		public Highlight(int start, int end, String text){
			this.start= start;
			this.end = end;
			this.text = text;
		}
	}
	
	
	public Editor(){
		highlightedTexts = new LinkedList<Highlight>();
		
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem openFileMenuItem = new JMenuItem("Open File");
		openFileMenuItem.addActionListener(this);
		
		JMenuItem openSavedMenuItem = new JMenuItem("Open Saved File");
		openSavedMenuItem.addActionListener(this);
		
		
		JMenuItem saveMenuItem = new JMenuItem("Save");
		saveMenuItem.addActionListener(this);
		
		JMenuItem closeMenuItem = new JMenuItem("Close");
		saveMenuItem.addActionListener(this);
		
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(this);
		
		fileMenu.add(openFileMenuItem);
		fileMenu.add(openSavedMenuItem);
		fileMenu.add(closeMenuItem);
		fileMenu.add(saveMenuItem);
		fileMenu.add(exitMenuItem);
		menuBar.add(fileMenu);
		
		JToolBar toolBar = new JToolBar();
		highLightButton = new JButton("Highlight");
		//highLightButton.setSize(new Dimension(100,50));
		highLightButton.addActionListener(this);
		
		ImageIcon noteIcon = new ImageIcon("./icons/NotesIcon.png");
		
		JButton addNoteButton = new JButton(noteIcon);
		addNoteButton.setText("Add Note");
		addNoteButton.addActionListener(this);
		
		
		
		printHighlightedButton = new JButton("Print Highlighted");
		//printHighlightedButton.setSize(new Dimension(100,20));
		printHighlightedButton.addActionListener(this);
		
		pageNumberBox = new JTextField(20);
		//pageNumberBox.setSize(15, 10);
		pageNumberBox.setMaximumSize(new Dimension(28,28));
		pageNumberBox.addActionListener(this);
		
		JButton goPageButton = new JButton("Go");
		//highLightButton.setSize(new Dimension(100,50));
		goPageButton.addActionListener(this);
		
		toolBar.add(highLightButton);
		toolBar.add(addNoteButton);
		toolBar.add(printHighlightedButton);
		toolBar.add(pageNumberBox);
		toolBar.add(goPageButton);
		
		mainEditorPane = this.getContentPane(); 
		this.setJMenuBar(menuBar);
		
		mainEditorPane.add(toolBar, BorderLayout.NORTH);
		

		setLocation(150, 20);
		setSize( mainWindowWidth, mainWindowHeight);
		setTitle("Campus Reader");
		setVisible(true);
	}
	
	
	public void WriteXML() throws IOException
    {
    
    JFileChooser fc= new JFileChooser();
    int return_value=fc.showSaveDialog(Editor.this);
    
    
    
    if(return_value==JFileChooser.APPROVE_OPTION)
    {
    	File save_file= fc.getSelectedFile();
    
    	String path = save_file.getPath();
     
   	  //File saveFile= new File("./res/New_Task.xml");
      
      FileWriter fwrite=new FileWriter(save_file);
      BufferedWriter out =new BufferedWriter(fwrite);
      
      out.write("<?xml version=\"1.0\"?>");
      out.newLine();
      
      out.write("<FILE path=\""+filePath+"\">");
      out.newLine();
      int i=0;
      for(Highlight h: highlightedTexts){
    	  ++i;
    	  
    	  out.write("<HIGHLIGHT index=\""+i+"\">");
    	  out.newLine();
    	
    	  out.write("<START_POS>");
    	  out.write(""+h.start+"");
    	  out.write("</START_POS>");
    	  out.newLine();
      
    	  out.write("<END_POS>");
    	  out.write(""+h.end+"");
    	  out.write("</END_POS>");
    	  out.newLine();
    	  
    	  out.write("<TEXT>");
    	  out.write(""+h.text+"");
    	  out.write("</TEXT>");
    	  out.newLine();
    	  
    	  out.write("</HIGHLIGHT>");
    	  out.newLine();
      }
      out.write("</FILE>");      
      out.close();
      
    
    }
    
    }
	
	
	
	public static void main(String[] args) {
	
		Editor e = new Editor();
		
		Thread editorThread = new Thread(e);
		editorThread.start();
		
		

	}

	
	public JScrollPane createChapterOutline(){
		
		leftPanel = new JPanel();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Chapter Outline");
		JTree outlineTree = new JTree(root);
		
		DefaultMutableTreeNode htmlBasic = new DefaultMutableTreeNode("HTML Basics");
		DefaultMutableTreeNode htmlTags = new DefaultMutableTreeNode("HTML Tags");
		htmlBasic.add(htmlTags);
		DefaultMutableTreeNode htmlElements = new DefaultMutableTreeNode("HTML Elements");
		htmlBasic.add(htmlElements);
		root.add(htmlBasic);
		
		
		DefaultMutableTreeNode docFormat = new DefaultMutableTreeNode("Document Formatting");
		root.add(docFormat);
		
		DefaultMutableTreeNode htextMedia = new DefaultMutableTreeNode("Hypertext and Media");
		root.add(htextMedia);
		
		DefaultMutableTreeNode lists = new DefaultMutableTreeNode("Lists");
		root.add(lists);
		
		DefaultMutableTreeNode tables = new DefaultMutableTreeNode("Tables");
		root.add(tables);
		
		DefaultMutableTreeNode pageAccess = new DefaultMutableTreeNode("Making Pages Accessible");
		root.add(pageAccess);
		
		DefaultMutableTreeNode lookAhead = new DefaultMutableTreeNode("Looking Ahead...");
		root.add(lookAhead);
		
		DefaultMutableTreeNode chSummary = new DefaultMutableTreeNode("Chapter Summary");
		root.add(chSummary);
		
		DefaultMutableTreeNode sMaterialsEx = new DefaultMutableTreeNode("Supplemental Material and Exercises");
		root.add(sMaterialsEx);
		
		leftPanel.add(outlineTree);
		leftPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		//leftPanel.setPreferredSize(new Dimension(150,400));
		JScrollPane leftSPane = new JScrollPane(leftPanel);
		//leftSPane.add(leftPanel);
		//leftPanel.add(new JLabel("Chapter Outline"));
		return leftSPane;
		
		
	}
	
	

	@Override
	public void actionPerformed(ActionEvent ae) {
		String eventName = ae.getActionCommand();
		
		if(eventName.equals("Open File"))
		{
			//filePath = "./pages/New_Task.htm";
			filePath = "./pages/Archaeology.htm";
			/*
			final JFileChooser fc = new JFileChooser();
			int returnValue = fc.showOpenDialog(this);
			if(returnValue == JFileChooser.APPROVE_OPTION);
			File file = fc.getSelectedFile();
			filePath = file.getPath();//file.getName();
			*/
			
			URL fileURL = null;
			
			try {
				//fileURL = new URL("file:./pages/New_Task_hLink.htm");
				//fileURL = new URL("file:./pages/Archaeology.htm");
				fileURL = new URL("file:./pages/Archaeology_ch_1.html");
				//fileURL = new URL("file:/"+filePath);
				
				//String file = "file:./"+filePath;
				
					//file = "file:./";
				
				//fileURL = new URL("file:/"+file);
				editorPane = new JEditorPane(fileURL);
				
				
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			editorPane.setEditable(false);
			
			editorPane.addHyperlinkListener(new HyperlinkListener()
	        {
	            public void hyperlinkUpdate(HyperlinkEvent r)
	            {
	                try
	                {
		             if(r.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
		             editorPane.setPage(r.getURL());
	                }
	                catch(Exception e)
	                {
	                e.printStackTrace();	
	                }
	            }
	        });

			
			
			htr = editorPane.getHighlighter();
			
			editorPane.setSelectionColor(Color.YELLOW);

			try {
				
				htr.addHighlight(20, 40, DefaultHighlighter.DefaultPainter);
				
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			JScrollPane scrollPane = new JScrollPane(editorPane);
			scrollPane.getVerticalScrollBar().addAdjustmentListener(this);
			
			scrollPane.setPreferredSize(new Dimension(550, 400));
			mainEditorPane.add(scrollPane, BorderLayout.CENTER);
	
			JScrollPane leftScrollPane = createChapterOutline();
			leftScrollPane.setPreferredSize(new Dimension(200,400));
			mainEditorPane.add(leftScrollPane, BorderLayout.WEST);
			
			String rightPanelFile = "./pages/right_side_text.html";
			
			try {
				rightNotePane = new JEditorPane(new URL("file:"+rightPanelFile));
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			JScrollPane rightScrollPane = new JScrollPane(rightNotePane);
			leftScrollPane.setPreferredSize(new Dimension(200,400));
			mainEditorPane.add(rightScrollPane, BorderLayout.EAST);
			
			
			bottomPanel = new JPanel();
			//bottomPanel.setSize(1000, 300);
			bottomPanel.setBackground(Color.WHITE);
			
			
			ImageIcon summaryImage = new ImageIcon("./res/photos/summary.jpg");
			JLabel summaryLabel = new JLabel();
			summaryLabel.setIcon(summaryImage);
			summaryLabel.setText("<html>" +
					"Chapter Summary<br/>" +
					"<ul>" +
					"<li>" +
					"A web page is a text document that contains additional formatting information in a lan-" +
					"guage called HTML(HyperText Markup Language). A Web browser is a <br>" +
					"program that displays Web pages by interpreting the HTML and formatting the page accordingly. A Web" +
					"server is a computer that runs special software for <br/>" +
					"storing and retrieving pages. A Web address(formally known as a Uniform Resource Locatior, or URL) specifies the location of a particular Web page." +
					"<br/><li>" +
					"HTML specifies formatting within a page using tags. An HTML element, the building block of Web pages, is made up of text enclosed in tags that indicate the <br/>" +
					"text's role or purpose within the page." +
					"<br/><li>" +
					"Every HTML document must begin with the tag &lt;html&gt; and end with the tag &lt;/html&gt;." +
					"<li>" +
					"An HTML document has two main sections, the HEAD and the BODY. The HEAD contains the TITLE of the page, which appears at the top the browser window <br>" +
					"when that page is displayed. The BODY contains the text and formatting that you want to appear within the page." +
					"</ul>" +
					"</html>");
			
			bottomPanel.add(summaryLabel, BorderLayout.CENTER);
			
			bottomPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			JScrollPane bottomSPanel = new JScrollPane(bottomPanel);
			bottomSPanel.setPreferredSize(new Dimension(1000,150));
			//mainEditorPane.add(bottomSPanel, BorderLayout.SOUTH);
			
			//added scrollPane in runtime
			this.validate();
			//this.repaint();
			
		}
		
		if(eventName.equals("Highlight")){
			
			if(!isSetHighlight){
					isSetHighlight = true;
					
					highLightButton.setBackground(Color.YELLOW);
			}
			else{
				isSetHighlight = false;
				highLightButton.setBackground(new Color(238,238,238));
			}
		}
		
		
		
		if(eventName.equals("Add Note")){
			

			Container c = this.getContentPane();
			if(!isSetAddNote){
				c.setCursor(new Cursor(Cursor.HAND_CURSOR));
				isSetAddNote = true;
			}
			else{
				c.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));	
				isSetAddNote = false;
			}
		}
		
		
		
		if(eventName.equals("Print Highlighted")){
			
			for(Highlight h: highlightedTexts){
				out.println(h.text);
			}
		}
		
		if(eventName.equals("Go")){
			int pageNumber = Integer.parseInt(pageNumberBox.getText());
			String page = "page"+pageNumber;
			editorPane.scrollToReference(page);
			rightNotePane.scrollToReference(page);
		}
		
		
		if(eventName.equals("Save"))
		{
			out.print("Saving..");
			//save the page with highlights and notes
			  try {
				WriteXML();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		
		if(eventName.equals("Open Saved File")){
		
			
			final JFileChooser fc = new JFileChooser();
			int returnValue = fc.showOpenDialog(this);
			String originalFilePath="";
			Element root = null;
			
			if(returnValue == JFileChooser.APPROVE_OPTION){
			File file = fc.getSelectedFile();
			//filePath = file.getPath();//file.getName();
			
			   SAXBuilder builder =new SAXBuilder();
			   Document doc;
			   
			    
			try {
				doc = builder.build(file);
				root =doc.getRootElement();
				originalFilePath = root.getAttributeValue("path");
				System.out.println(originalFilePath);
				
				
			} catch (JDOMException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			   

			
			URL fileURL = null;
			
			try {
				//fileURL = new URL("file:./pages/New_Task.htm");
				fileURL = new URL("file:"+originalFilePath);
				
				//String file = "file:./"+filePath;
				
					//file = "file:./";
				
				//fileURL = new URL("file:/"+file);
				editorPane = new JEditorPane(fileURL);
				
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			editorPane.setEditable(false);
			Highlighter htr1 = editorPane.getHighlighter();
			
			editorPane.setSelectionColor(Color.YELLOW);
			
			JScrollPane scrollPane = new JScrollPane(editorPane);
			mainEditorPane.add(scrollPane, BorderLayout.CENTER);
			
			List<Element> highlights =   root.getChildren("HIGHLIGHT");
			Iterator<Element> it = highlights.iterator();
			while(it.hasNext()){
			
				Element ht = (Element)it.next(); 
				int start = Integer.parseInt(ht.getChildText("START_POS"));
				int end = Integer.parseInt(ht.getChildText("END_POS"));
				System.out.print("s"+start+" e "+end);
				try {
					htr1.addHighlight(start, end, DefaultHighlighter.DefaultPainter);
					this.validate();
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			//added scrollPane in runtime
			this.validate();
		
			
			
		}
		
		}
		
		
		
		
		if(eventName.equals("Exit"))
		{
			System.exit(0);
			
		}
		
	}



	@Override
	public void run() {
		int start, oldStart = -1;
		int end, oldEnd =-1;
		while(true){
			if(editorPane!=null){
				String text = editorPane.getSelectedText();
				start = editorPane.getSelectionStart();
				end = editorPane.getSelectionEnd();
				
				//highlighted.add(new Highlight(start, end, text));
				
				try {
					if(isSetHighlight)
					if(htr!=null && oldStart!=start && oldEnd!=end){
						htr.addHighlight(start, end, DefaultHighlighter.DefaultPainter);
					if(text!=null)
						highlightedTexts.add(new Highlight(start, end, text));
				
					System.out.println(start);
					oldStart=start;
					oldEnd= end;
					
					if(text!=null)
						out.println(text);
					}
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			
			
			}
		
		}
	}

	static int lastPage=1;

	@Override
	public void adjustmentValueChanged(AdjustmentEvent ae) {
		scrollValue = ae.getValue();
		System.out.println(scrollValue);
		int pageNum = (scrollValue/1000)+1;
		
		System.out.println("page no. "+pageNum);
		
		if(lastPage!=pageNum){
		String page = "page"+pageNum;
		editorPane.scrollToReference(page);
		rightNotePane.scrollToReference(page);
		}
		lastPage = pageNum;
		
	}

}
