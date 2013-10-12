package edu.cmu.side.view.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.Workbench;
import edu.cmu.side.control.GenesisControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.RecipeManager.Stage;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.plugin.control.ImportController;
import edu.cmu.side.recipe.converters.ConverterControl;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.RecipeExporter;

public abstract class GenericLoadPanel extends AbstractListPanel
{


	protected JPanel describePanel;
	protected JLabel label;
	protected JButton warn = new JButton("");
	protected JFileChooser chooser;
	protected JPanel buttons = new JPanel(new RiverLayout(0, 0));


	//TODO: reconcile this with RecipeExporter.
//	public static FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV", "csv", "CSV");
//	public static FileNameExtensionFilter arffFilter = new FileNameExtensionFilter("ARFF (Weka)", "arff");
//	public static FileNameExtensionFilter sideFilter = new FileNameExtensionFilter("LightSide", "side");
//	public static FileNameExtensionFilter predictFilter = new FileNameExtensionFilter("Predict-Only Model", "predict", "predict.side");

	protected GenericLoadPanel()
	{

		setLayout(new RiverLayout());
		combo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				if (combo.getSelectedItem() != null)
				{
					Recipe r = (Recipe) combo.getSelectedItem();
					setHighlight(r);

					Component recipeTree = GenesisControl.getRecipeTree(getHighlight());
					describeScroll = new JScrollPane(recipeTree);
					if(describePanel != null)
					{
						describePanel.removeAll();
						describePanel.add(BorderLayout.CENTER, describeScroll);
	
						describePanel.revalidate();
					}
				}
				Workbench.update(GenericLoadPanel.this);

			}
		});
	}

	//not directly connected to setHighlight or getHighlight - just the UI.
	public Recipe getSelectedItem()
	{
		if (combo.getSelectedItem() != null)
		{
			return (Recipe) combo.getSelectedItem();
		}
		return null;
		
	}
	
	//not directly connected to setHighlight or getHighlight - just the UI.
	public void setSelectedItem(Recipe r)
	{
		if (r != null)
		{
			combo.setSelectedItem(r);
			save.setEnabled(true);
			combo.setEnabled(true);
			delete.setEnabled(true);
		}
		else
		{
			combo.setEnabled(false);
			combo.setSelectedIndex(-1);
			save.setEnabled(false);
			delete.setEnabled(false);
			describeScroll = new JScrollPane();
			if (describePanel != null)
			{
				describePanel.removeAll();
				describePanel.add(BorderLayout.CENTER, describeScroll);
			}
		}
	}

	public GenericLoadPanel(String l)
	{
		this(l, true, true, true);
	}

	public GenericLoadPanel(String l, boolean showLoad, boolean showDelete, boolean showSave)
	{
		this(l, showLoad, showDelete, showSave, true);
	}

	public GenericLoadPanel(String l, boolean showLoad, boolean showDelete, boolean showSave, boolean showDescription)
	{
		this();

		label = new JLabel(l);
		ImageIcon iconDelete = new ImageIcon("toolkits/icons/cross.png");
		ImageIcon iconSave = new ImageIcon("toolkits/icons/disk.png");
		ImageIcon iconLoad = new ImageIcon("toolkits/icons/folder_table.png");
		ImageIcon iconWarn = new ImageIcon("toolkits/icons/error.png");
		//TODO: replace with WarningButton utility class
		warn.setIcon(iconWarn);
		warn.setBorderPainted(false);
		warn.setContentAreaFilled(false);
		warn.setOpaque(false);
		warn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(GenericLoadPanel.this, warn.getToolTipText(), "Warning", JOptionPane.WARNING_MESSAGE);
			}
		});
		warn.setVisible(false);

		delete.setText("");
		delete.setIcon(iconDelete);
		delete.setToolTipText("Delete");
		delete.setBorderPainted(true);
		delete.setEnabled(false);
		save.setText("");
		save.setIcon(iconSave);
		save.setToolTipText("Save");
		save.setBorderPainted(true);
		save.setEnabled(false);
		load.setText("");
		load.setIcon(iconLoad);
		load.setToolTipText("Load");
		load.setBorderPainted(true);

		buttons.setBorder(BorderFactory.createEmptyBorder());
		
		add("hfill", label);
		buttons.add("right", warn);
		if (showLoad) buttons.add("right", load);
		add("right", buttons);
		add("br hfill", combo);
		if (showSave) add("right", save);
		if (showDelete) add("right", delete);

		if (showDescription)
		{
			describePanel = new JPanel(new BorderLayout());
			describeScroll = new JScrollPane();
			describePanel.add(BorderLayout.CENTER, describeScroll);
			add("br hfill vfill", describePanel);
		}
		// add("br left hfill", buttons);

		connectButtonListeners();
		// GenesisControl.addListenerToMap(this, this);
	}

	public abstract void setHighlight(Recipe r);

	public abstract Recipe getHighlight();

	public void deleteHighlight()
	{
		describeScroll = new JScrollPane();
		setHighlight(null);
	}

	@Override
	public abstract void refreshPanel();

	public void refreshPanel(Collection<Recipe> recipes)
	{
		if (combo.getItemCount() != recipes.size())
		{
			Workbench.reloadComboBoxContent(combo, recipes, getHighlight());
		}
		if (getHighlight() == null && combo.getItemCount() > 0)
		{
			Recipe r = (Recipe) combo.getItemAt(combo.getItemCount() - 1);
			setHighlight(r);
		}
		if (getHighlight() != null && !Workbench.getRecipeManager().containsRecipe(getHighlight()))
		{
			deleteHighlight();
		}
		if (getHighlight() != null)
		{
			combo.setSelectedItem(getHighlight());
			save.setEnabled(true);
			combo.setEnabled(true);
			delete.setEnabled(true);
		}
		else
		{
			combo.setEnabled(false);
			combo.setSelectedIndex(-1);
			save.setEnabled(false);
			delete.setEnabled(false);
			describeScroll = new JScrollPane();
			if (describePanel != null)
			{
				describePanel.removeAll();
				describePanel.add(BorderLayout.CENTER, describeScroll);
			}
		}
	}

	/** load/save/delete button listeners */
	private void connectButtonListeners()
	{
		save.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				save.setEnabled(false);
				if (combo.getSelectedIndex() >= 0)
				{
					SwingWorker saver = new SwingWorker()
					{

						@Override
						protected Object doInBackground() throws Exception
						{
							saveSelectedItem();
							return null;
						}
						
						@Override
						public void done()
						{
							save.setEnabled(true);
						}
						
					};
					
					saver.execute();
				}
			}

		});

		delete.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (combo.getSelectedIndex() >= 0)
				{
					deleteSelectedItem();
				}
			}
		});

		load.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadNewItem();
			}

		});
	}

	public void saveSelectedItem()
	{
		checkChooser();
		
		Recipe recipe = (Recipe) combo.getSelectedItem();
		System.out.println("saving "+recipe+" ("+recipe.getStage()+")");

		if (recipe.getStage() == Stage.FEATURE_TABLE || recipe.getStage() == Stage.MODIFIED_TABLE)
		{
			RecipeExporter.exportFeatures(recipe);
		}
		else if (recipe.getStage() == Stage.TRAINED_MODEL || recipe.getStage() == Stage.PREDICTION_ONLY)
		{
			RecipeExporter.exportTrainedModel(recipe);
		}
		else
		{
			chooser.setFileFilter(RecipeExporter.getGenericFilter());
			chooser.setSelectedFile(new File("saved/" + recipe.getRecipeName()));
			int response = chooser.showSaveDialog(this);
			if (response == JFileChooser.APPROVE_OPTION)
			{
				File target = chooser.getSelectedFile();
				if (target.exists())
				{
					response = JOptionPane.showConfirmDialog(this, target.getName() + " already exists in this folder.\nDo you want to overwrite it?");
					if (response != JOptionPane.YES_OPTION) return;
				}

				try
				{
					if(!RecipeExporter.useXML())
					{
						FileOutputStream fout = new FileOutputStream(target);
						ObjectOutputStream oos = new ObjectOutputStream(fout);
						oos.writeObject(recipe);
					}
					else
					{
						ConverterControl.writeToXML(target, recipe);
					}

				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(this, "Error while saving:\n" + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}

			}
		}
	}

	public void deleteSelectedItem()
	{
		Recipe recipe = (Recipe) combo.getSelectedItem();// TODO: should this be
															// more generic?
		setHighlight(null);
		Workbench.getRecipeManager().deleteRecipe(recipe);
		Workbench.update(this);
	};

	public void loadNewItem()
	{
		checkChooser();
		
		switch(getLoadableStage())
		{
			case TRAINED_MODEL:
			case PREDICTION_RESULT:
				chooser.setFileFilter(RecipeExporter.getTrainedModelFilter());
				break;
			case FEATURE_TABLE:
			case MODIFIED_TABLE:
				chooser.setFileFilter(RecipeExporter.getFeatureTableFilter());
				break;
			case PREDICTION_ONLY:
				chooser.setFileFilter(RecipeExporter.getPredictModelFilter());
				break;
			default:
				chooser.setFileFilter(RecipeExporter.getGenericFilter());
		}
		
		int response = chooser.showOpenDialog(this);
		if (response == JFileChooser.APPROVE_OPTION)
		{
			File target = chooser.getSelectedFile();
			if (!target.exists())
			{
				JOptionPane.showMessageDialog(this, "The selected file does not exist. Where did it go?", "No Such File", JOptionPane.ERROR_MESSAGE);
			}

			try
			{
				Recipe recipe;
				recipe = ConverterControl.loadRecipe(target.getPath());
//				if(RecipeExporter.useXML())
//				{
//					recipe = ConverterControl.readFromXML(target);
//				}
//				else
//				{
//					FileInputStream fout = new FileInputStream(target);
//					ObjectInputStream in = new ObjectInputStream(fout);
//					recipe = (Recipe) in.readObject();
//				}
				Workbench.getRecipeManager().addRecipe(recipe);
				setHighlight(recipe);
				Workbench.update(this);
				Workbench.update(recipe.getStage());
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, "Error while loading file:\n" + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}

		}
	}

	protected void loadNewDocumentsFromCSV()
	{
		checkChooser();
		
		chooser.setFileFilter(RecipeExporter.getCSVFilter());
		chooser.setMultiSelectionEnabled(true);
		int result = chooser.showOpenDialog(GenericLoadPanel.this);
		if (result != JFileChooser.APPROVE_OPTION) { return; }

		File[] selectedFiles = chooser.getSelectedFiles();
		TreeSet<String> docNames = new TreeSet<String>();

		for (File f : selectedFiles)
		{
			docNames.add(f.getPath());
		}
		try{
		DocumentList testDocs = ImportController.makeDocumentList(docNames);
		testDocs.guessTextAndAnnotationColumns();
		Recipe r = Workbench.getRecipeManager().fetchDocumentListRecipe(testDocs);
		setHighlight(r);
		}catch(FileNotFoundException e){
			JOptionPane.showMessageDialog(this, e.getMessage(), "File Not Found", JOptionPane.ERROR_MESSAGE);
		}catch(Exception e){
			JOptionPane.showMessageDialog(this, e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		refreshPanel();
		Workbench.update(this);
	}

	protected void checkChooser()
	{
		if(chooser == null)
		{
			chooser = new JFileChooser(new File("saved"));
		}
	}

	public void setWarning(String warnText)
	{
		warn.setVisible(true);
		warn.setToolTipText(warnText);
	}

	public void clearWarning()
	{
		warn.setVisible(false);
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		
		combo.setEnabled(enabled);
		load.setEnabled(enabled);
		delete.setEnabled(enabled);
		save.setEnabled(enabled);
		warn.setEnabled(enabled);
		
		for(Component c : describeScroll.getComponents())
		{
			c.setEnabled(enabled);
			if(c instanceof Container)
			for(Component cc : ((Container)c).getComponents())
			{
				cc.setEnabled(enabled);
			}
		}
	}
	
	public abstract Stage getLoadableStage();
}
