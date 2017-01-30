/*
 * Copyright 2016 Florian Pollak
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.bfour.fpliteraturecollector.gui.literature;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import com.github.bfour.fpliteraturecollector.domain.Author;
import com.github.bfour.fpliteraturecollector.domain.Literature;
import com.github.bfour.fpliteraturecollector.domain.Tag;
import com.github.bfour.fpliteraturecollector.domain.builders.LiteratureBuilder;
import com.github.bfour.fpliteraturecollector.service.LiteratureService;
import com.github.bfour.fpliteraturecollector.service.ServiceManager;
import com.github.bfour.jlib.commons.logic.ContainsExpression;
import com.github.bfour.jlib.commons.logic.LogicException;
import com.github.bfour.jlib.commons.logic.OrExpression;
import com.github.bfour.jlib.commons.services.ServiceException;
import com.github.bfour.jlib.commons.utils.Getter;
import com.github.bfour.jlib.gui.abstraction.EntityFilterPipeline;
import com.github.bfour.jlib.gui.abstraction.feedback.Feedback;
import com.github.bfour.jlib.gui.abstraction.feedback.Feedback.FeedbackType;
import com.github.bfour.jlib.gui.abstraction.valueChangeHandling.ValueChangeEvent;
import com.github.bfour.jlib.gui.abstraction.valueChangeHandling.ValueChangeListener;
import com.github.bfour.jlib.gui.components.FPJGUIButton;
import com.github.bfour.jlib.gui.components.FPJGUIButton.ButtonFormats;
import com.github.bfour.jlib.gui.components.FPJGUIButton.FPJGUIButtonFactory;
import com.github.bfour.jlib.gui.components.FPJGUILabel;
import com.github.bfour.jlib.gui.components.FPJGUIPopover;
import com.github.bfour.jlib.gui.components.composite.EntityCheckboxTreeBrowsePanel;
import com.github.bfour.jlib.gui.components.composite.EntityTableBrowsePanel;
import com.github.bfour.jlib.gui.components.table.FPJGUITable.FPJGUITableFieldGetter;
import com.github.bfour.jlib.gui.components.table.FPJGUITableColumn;
import com.github.bfour.jlib.gui.design.Icons;
import com.github.bfour.jlib.gui.design.Lengths;
import com.github.bfour.jlib.gui.events.SelectionChangeEvent;
import com.github.bfour.jlib.gui.events.SelectionChangeSubscriber;
import com.github.bfour.jlib.guiextended.tagging.TaggingPanel;
import com.github.bfour.jlib.search.SearchEvent;
import com.github.bfour.jlib.search.SearchException;
import com.github.bfour.jlib.search.lang.SearchSpecification;

public class LiteratureBrowsePanel extends EntityTableBrowsePanel<Literature> {

	private class DownloaderWorker extends SwingWorker<Void, Void> {

		private ServiceManager servMan;
		private Component button;

		public DownloaderWorker(Component button, ServiceManager servMan) {
			this.button = button;
			this.servMan = servMan;
		}

		@Override
		protected Void doInBackground() throws Exception {
			List<Literature> selectedLiterature = getValue();
			Feedback statusFeedback = new Feedback(LiteratureBrowsePanel.this,
					"Downloading fulltext for " + selectedLiterature.size()
							+ " literature entries.", FeedbackType.PROGRESS);
			feedbackBroadcasted(statusFeedback);
			long count = 0;
			for (Literature lit : selectedLiterature) {
				try {
					servMan.getLiteratureService().downloadFullTexts(lit);
				} catch (Exception e1) {
					// feedbackBroadcasted(new Feedback(
					// LiteratureBrowsePanel.this,
					// "Sorry, failed to download fulltext for literature ID "
					// + lit.getID(), e1.getMessage(),
					// FeedbackType.ERROR));
				}
				count++;
				Feedback newStatusFeedback = new Feedback(
						LiteratureBrowsePanel.this, count + " of "
								+ selectedLiterature.size()
								+ " fulltexts downloaded.",
						FeedbackType.PROGRESS);
				feedbackChanged(statusFeedback, newStatusFeedback);
				statusFeedback = newStatusFeedback;
			}
			feedbackRevoked(statusFeedback);
			feedbackBroadcasted(new Feedback(LiteratureBrowsePanel.this,
					"Fulltext download finished for "
							+ selectedLiterature.size()
							+ " literature entries.", FeedbackType.SUCCESS));
			button.setEnabled(true);
			return null;
		}

	}

	private static final long serialVersionUID = 4500980555674670335L;

	public LiteratureBrowsePanel(final ServiceManager servMan) {
		this(servMan, new EntityFilterPipeline<Literature>());
	}

	public LiteratureBrowsePanel(final ServiceManager servMan,
			EntityFilterPipeline<Literature> filters) {

		super(Literature.class, servMan.getLiteratureService(), false);

		setFilters(filters);

		final TaggingPanel<Tag, Literature> taggingPanel = new TaggingPanel<>(
				Tag.class, servMan.getTagService());
		final FPJGUIPopover tagPopover = new FPJGUIPopover(taggingPanel);

		taggingPanel.addCancelListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tagPopover.hidePopup();
			}
		});
		taggingPanel.addConfirmListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<Tag> tags = taggingPanel.getTags();
				LiteratureService litServ = servMan.getLiteratureService();
				int successCounter = 0;
				for (Literature selectedLit : getValue()) {
					Literature newLiterature = new LiteratureBuilder(
							selectedLit).setTags(new HashSet<Tag>(tags))
							.getObject();
					try {
						litServ.update(selectedLit, newLiterature);
						successCounter++;
					} catch (ServiceException e1) {
						feedbackBroadcasted(new Feedback(
								LiteratureBrowsePanel.this,
								"Sorry, failed to set tags for " + selectedLit,
								e1.getMessage(), FeedbackType.ERROR));
					}
				}
				feedbackBroadcasted(new Feedback(LiteratureBrowsePanel.this,
						"Tags for " + successCounter
								+ " literature entries set.",
						FeedbackType.SUCCESS));
				tagPopover.hidePopup();
			}
		});

		// show default buttons for CRUD options
		setEditEntityEnabled(false);
		setCreateEntityEnabled(true);
		setDeleteEntityEnabled(true);

		// extra buttons
		FPJGUIButton exportButton = FPJGUIButtonFactory
				.createButton(
						ButtonFormats.DEFAULT,
						Lengths.LARGE_BUTTON_HEIGHT.getLength(),
						"Export to MODS",
						com.github.bfour.fpliteraturecollector.gui.design.Icons.EXPORT_20
								.getIcon());
		getMainPanel().add(exportButton, "cell 0 2");
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<Literature> selectedLiterature = getValue();
				Feedback statusFeedback = new Feedback(
						LiteratureBrowsePanel.this, "Exporting "
								+ selectedLiterature.size()
								+ " literature entries to MODS.", "",
						FeedbackType.PROGRESS.getColor(), FeedbackType.PROGRESS
								.getIcon(), FeedbackType.PROGRESS, true);
				feedbackBroadcasted(statusFeedback);
				try {
					JFileChooser fileChooser = new JFileChooser();
					if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();
						servMan.getReportService().exportToMODSFile(
								selectedLiterature, file);
						feedbackBroadcasted(new Feedback(
								LiteratureBrowsePanel.this,
								"Export to MODS finished for "
										+ selectedLiterature.size()
										+ " literature entries.",
								FeedbackType.SUCCESS));
					} else {
						feedbackBroadcasted(new Feedback(
								LiteratureBrowsePanel.this,
								"Export to MODS cancelled.", FeedbackType.WARN));
					}
				} catch (FileNotFoundException e1) {
					feedbackBroadcasted(new Feedback(
							LiteratureBrowsePanel.this,
							"Sorry, export to MODS failed, because export file not found.",
							e1.getMessage(), FeedbackType.ERROR));
				}
				feedbackRevoked(statusFeedback);
			}
		});

		FPJGUIButton downloadFullTextButton = FPJGUIButtonFactory
				.createButton(
						ButtonFormats.DEFAULT,
						Lengths.LARGE_BUTTON_HEIGHT.getLength(),
						"Download Fulltext",
						com.github.bfour.fpliteraturecollector.gui.design.Icons.DOWNLOAD_20
								.getIcon());
		getMainPanel().add(downloadFullTextButton, "cell 0 2");
		downloadFullTextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				downloadFullTextButton.setEnabled(false);
				DownloaderWorker worker = new DownloaderWorker(
						downloadFullTextButton, servMan);
				worker.execute();
			}
		});

		final FPJGUIButton tagButton = FPJGUIButtonFactory.createButton(
				ButtonFormats.DEFAULT, Lengths.LARGE_BUTTON_HEIGHT.getLength(),
				"Tag",
				com.github.bfour.fpliteraturecollector.gui.design.Icons.TAG_16
						.getIcon());
		getMainPanel().add(tagButton, "cell 0 2");
		ActionListener taggingActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<Literature> selectedLiterature = getValue();
				taggingPanel.setValue(selectedLiterature);
				Set<Tag> allTags = new HashSet<Tag>();
				for (Literature lit : selectedLiterature) {
					Set<Tag> litTags = lit.getTags();
					if (litTags != null)
						allTags.addAll(litTags);
				}
				taggingPanel.setTags(new ArrayList<>(allTags));
				tagPopover.pack();
				tagPopover.showPopup(tagButton);
			}
		};
		tagButton.addActionListener(taggingActionListener);
		getListLikeContainer().addAction("Tag",
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK),
				taggingActionListener);

		// semantic tagging
		SemanticTaggingPopover semanticsPopover = new SemanticTaggingPopover(
				servMan, new Getter<Void, Literature>() {
					@Override
					public Literature get(Void arg0) {
						return getListLikeContainer().getSelectedItem();
					}

				});

		final FPJGUIButton semanticsButton = FPJGUIButtonFactory.createButton(
				ButtonFormats.DEFAULT, Lengths.LARGE_BUTTON_HEIGHT.getLength(),
				"Semantic Tagging",
				com.github.bfour.fpliteraturecollector.gui.design.Icons.TAG_16
						.getIcon());
		getMainPanel().add(semanticsButton, "cell 0 2");
		ActionListener semanticTaggingActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<Literature> selectedLiterature = getValue();
				if (selectedLiterature.isEmpty()) {
					feedbackBroadcasted(new Feedback(semanticsButton,
							"Please select a literature entry.",
							FeedbackType.WARN));
					return;
				}
				if (selectedLiterature.size() > 1) {
					feedbackBroadcasted(new Feedback(semanticsButton,
							"Please select only one literature entry.",
							FeedbackType.WARN));
					return;
				}
				semanticsPopover.setValue(selectedLiterature.get(0));
				semanticsPopover.pack();
				semanticsPopover.showPopup(tagButton);
			}
		};
		semanticsButton.addActionListener(semanticTaggingActionListener);
		getListLikeContainer().addAction("Semantic Tagging",
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK),
				semanticTaggingActionListener);

		addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChanged(ValueChangeEvent event) {
				List<Literature> selection = getListLikeContainer()
						.getSelectedItems();
				if (selection.isEmpty() || selection.size() > 1) {
					semanticsButton.setIcon(null);
					semanticsButton.setEnabled(false);
				} else {
					semanticsButton.setEnabled(true);
					Literature lit = selection.get(0);
					try {
						SemanticValidator val = SemanticValidator
								.getInstance(servMan);
						if (!val.isValid(lit))
							semanticsButton.setIcon(Icons.EXCLAMATION_20
									.getIcon());
						else if (!val.isComplete(lit))
							semanticsButton.setIcon(Icons.ORANGEFLAG_20
									.getIcon());
						else
							semanticsButton.setIcon(Icons.GREENTICK_20
									.getIcon());
					} catch (LogicException e) {
						e.printStackTrace();
						feedbackBroadcasted(new Feedback(semanticsButton,
								"Failed to evaluate semantic tagging.",
								e.getMessage(), FeedbackType.ERROR));
					}
				}

			}
		});

		// selection mode
		getListLikeContainer().setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// additional search controls
		EntityCheckboxTreeBrowsePanel<Tag> categoryTree = new EntityCheckboxTreeBrowsePanel<>(
				Tag.class, new Getter<Tag, Tag>() {
					@Override
					public Tag get(Tag input) {
						return null;
					}
				}, servMan.getTagService(), true);
		categoryTree.setEditEntityEnabled(false);
		categoryTree.setCreateEntityEnabled(false);
		categoryTree.setDeleteEntityEnabled(false);
		getSidebarPanel().add(categoryTree, "grow, w 2cm:4cm:");
		setSidebarVisible(true);

		categoryTree.getListLikeContainer().addTreeCheckingListener(
				new SelectionChangeSubscriber<Tag>() {
					@Override
					public void receive(SelectionChangeEvent<Tag> ev) {
						OrExpression orExpr = new OrExpression();
						for (Tag tag : ev.getEntity()) {
							orExpr.addExpression(new ContainsExpression("tags",
									tag));
						}
						try {
							getSearchHandler().searchPerformed(
									new SearchEvent<SearchSpecification>(this,
											new SearchSpecification(null,
													orExpr)));
						} catch (SearchException e) {
							e.printStackTrace();
							feedbackBroadcasted(new Feedback(
									categoryTree,
									"Sorry, something went wrong with your search.",
									e.getMessage(), FeedbackType.ERROR));
						}
					}
				});

		// ==== columns ====
		FPJGUITableColumn<Literature> idColumn = new FPJGUITableColumn<Literature>(
				"ID", new FPJGUITableFieldGetter<Literature>() {
					@Override
					public String get(Literature item) {
						return item.getID() + "";
					}
				}, true, 30, 30, "id", false);
		getListLikeContainer().addColumn(idColumn);

		FPJGUITableColumn<Literature> titleColumn = new FPJGUITableColumn<Literature>(
				"Title", new FPJGUITableFieldGetter<Literature>() {
					@Override
					public String get(Literature item) {
						return item.getTitle();
					}
				}, true, 30, 30, "title", false);
		getListLikeContainer().addColumn(titleColumn);

		FPJGUITableColumn<Literature> authorsColumn = new FPJGUITableColumn<Literature>(
				"Authors", new FPJGUITableFieldGetter<Literature>() {
					@Override
					public String get(Literature item) {
						Set<Author> authors = item.getAuthors();
						if (authors == null || authors.isEmpty())
							return "";
						StringBuilder builder = new StringBuilder();
						for (Author author : authors) {
							builder.append(author.getLastName());
							builder.append(", ");
						}
						return builder.substring(0, builder.length() - 2);
					}
				}, true, 30, 30, "authors", false);
		getListLikeContainer().addColumn(authorsColumn);

		FPJGUITableColumn<Literature> tagsColumn = new FPJGUITableColumn<Literature>(
				"Tags", new FPJGUITableFieldGetter<Literature>() {
					@Override
					public String get(Literature item) {
						Set<Tag> tags = item.getTags();
						if (tags == null || tags.isEmpty())
							return "";
						StringBuilder builder = new StringBuilder();
						for (Tag tag : tags) {
							builder.append(tag.getName());
							builder.append(", ");
						}
						return builder.substring(0, builder.length() - 2);
					}
				}, true, 30, 30, "tags", false);
		getListLikeContainer().addColumn(tagsColumn);

		FPJGUITableColumn<Literature> yearColumn = new FPJGUITableColumn<Literature>(
				"Year", new FPJGUITableFieldGetter<Literature>() {
					@Override
					public String get(Literature item) {
						if (item.getYear() == null)
							return "";
						return item.getYear() + "";
					}
				}, true, 30, 30, "year", false);
		getListLikeContainer().addColumn(yearColumn);

		FPJGUITableColumn<Literature> isDoneColumn = new FPJGUITableColumn<Literature>(
				"IsDone", new FPJGUITableFieldGetter<Literature>() {
					@Override
					public String get(Literature item) {
						try {
							if (SemanticValidator.getInstance(servMan)
									.isComplete(item)
									&& SemanticValidator.getInstance(servMan)
											.isValid(item))
								return "yes";
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return "no";
					}
				}, true, 30, 30, "isDone", false);
		getListLikeContainer().addColumn(isDoneColumn);

		FPJGUITableColumn<Literature> isCompleteColumn = new FPJGUITableColumn<Literature>(
				"IsComplete", new FPJGUITableFieldGetter<Literature>() {
					@Override
					public String get(Literature item) {
						try {
							if (SemanticValidator.getInstance(servMan)
									.isComplete(item))
								return "yes";
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return "no";
					}
				}, true, 19, 19, "isComplete", false);
		getListLikeContainer().addColumn(isCompleteColumn);

		FPJGUITableColumn<Literature> isValidColumn = new FPJGUITableColumn<Literature>(
				"IsValid", new FPJGUITableFieldGetter<Literature>() {
					@Override
					public String get(Literature item) {
						try {
							if (SemanticValidator.getInstance(servMan).isValid(
									item))
								return "yes";
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return "no";
					}
				}, true, 19, 19, "isValid", false);
		getListLikeContainer().addColumn(isValidColumn);

		getListLikeContainer().setPreferredColumnWidth(idColumn, 30);
		getListLikeContainer().setPreferredColumnWidth(titleColumn, 500);
		getListLikeContainer().setPreferredColumnWidth(authorsColumn, 50);
		getListLikeContainer().setPreferredColumnWidth(tagsColumn, 30);
		getListLikeContainer().setPreferredColumnWidth(yearColumn, 30);
		getListLikeContainer().setPreferredColumnWidth(isDoneColumn, 30);
		getListLikeContainer().setPreferredColumnWidth(isCompleteColumn, 30);
		getListLikeContainer().setPreferredColumnWidth(isValidColumn, 30);

		getListLikeContainer().setMinimumColumnWidth(idColumn, 30);
		getListLikeContainer().setMinimumColumnWidth(titleColumn, 100);
		getListLikeContainer().setMinimumColumnWidth(authorsColumn, 40);
		getListLikeContainer().setMinimumColumnWidth(tagsColumn, 30);
		getListLikeContainer().setMinimumColumnWidth(yearColumn, 30);
		getListLikeContainer().setMinimumColumnWidth(isDoneColumn, 30);
		getListLikeContainer().setMinimumColumnWidth(isCompleteColumn, 30);
		getListLikeContainer().setMinimumColumnWidth(isValidColumn, 30);

		// stats
		FPJGUILabel<String> statsLabel = new FPJGUILabel<>();
		getMainPanel().add(statsLabel, "cell 0 2");
		addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChanged(ValueChangeEvent event) {
				String text = "";
				List<Literature> selection = getListLikeContainer()
						.getSelectedItems();
				text += selection.size() + " selected";
				statsLabel.setText(text);
			}
		});

		load();

	}
}
