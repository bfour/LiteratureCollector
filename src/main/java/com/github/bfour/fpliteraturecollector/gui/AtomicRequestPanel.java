package com.github.bfour.fpliteraturecollector.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.github.bfour.fpjcommons.lang.BuilderFactory;
import com.github.bfour.fpjcommons.services.DatalayerException;
import com.github.bfour.fpjcommons.services.ServiceException;
import com.github.bfour.fpjcommons.services.CRUD.CRUDService;
import com.github.bfour.fpjcommons.services.CRUD.DataIterator;
import com.github.bfour.fpjcommons.utils.Getter;
import com.github.bfour.fpjgui.abstraction.EntityEditPanel;
import com.github.bfour.fpjgui.abstraction.valueContainer.ValidationRule;
import com.github.bfour.fpjgui.components.FPJGUILabel;
import com.github.bfour.fpjgui.components.FPJGUIMultilineLabel;
import com.github.bfour.fpjgui.components.FPJGUITextPane;
import com.github.bfour.fpjgui.components.SearchComboBox;
import com.github.bfour.fpjgui.components.ToggleEditFormComponent;
import com.github.bfour.fpjgui.components.composite.EntityBrowsePanel;
import com.github.bfour.fpjgui.components.table.FPJGUITable;
import com.github.bfour.fpjgui.design.Colors;
import com.github.bfour.fpjgui.util.ObjectGraphicalValueContainerMapper;
import com.github.bfour.fpliteraturecollector.domain.AtomicRequest;
import com.github.bfour.fpliteraturecollector.domain.builders.AtomicRequestBuilder;
import com.github.bfour.fpliteraturecollector.service.ServiceManager;
import com.github.bfour.fpliteraturecollector.service.crawlers.Crawler;

public class AtomicRequestPanel extends
		EntityEditPanel<AtomicRequest, AtomicRequestBuilder> {

	private static final long serialVersionUID = -6108218045598314837L;

	/**
	 * Create the panel.
	 */
	public AtomicRequestPanel(final ServiceManager servMan,
			final FPJGUITable<AtomicRequest> table) {

		super(new BuilderFactory<AtomicRequest, AtomicRequestBuilder>() {
			@Override
			public AtomicRequestBuilder getBuilder() {
				return new AtomicRequestBuilder();
			}

			@Override
			public AtomicRequestBuilder getBuilder(AtomicRequest entity) {
				return new AtomicRequestBuilder(entity);
			}

		}, new CRUDService<AtomicRequest>() {

			@Override
			public AtomicRequest create(AtomicRequest a) {
				table.addEntry(a);
				return a;
			}

			@Override
			public void delete(AtomicRequest a) throws ServiceException {
				table.deleteEntry(a);
			}

			@Override
			public boolean exists(AtomicRequest a) throws ServiceException {
				return table.containsEntry(a);
			}

			@Override
			public DataIterator<AtomicRequest> get() throws ServiceException {
				final Iterator<AtomicRequest> iter = table.getEntries()
						.iterator();
				return new DataIterator<AtomicRequest>() {
					@Override
					public boolean hasNext() throws DatalayerException {
						return iter.hasNext();
					}

					@Override
					public AtomicRequest next() throws DatalayerException {
						return iter.next();
					}

					@Override
					public void remove() throws DatalayerException {
						iter.remove();
					}
				};
			}

			@Override
			public List<AtomicRequest> getAll() throws ServiceException {
				return table.getEntries();
			}

			@Override
			public AtomicRequest update(AtomicRequest oldEntry,
					AtomicRequest newEntry) throws ServiceException {
				table.updateEntry(newEntry);
				return newEntry;
			}
		});

		setCRUDButtonsVisible(false);
		
		getContentPane().setLayout(
				new MigLayout("insets 0", "[grow]", "[]8[]0[]8[]0[]"));

		JLabel dummy = new JLabel();
		Font labelFont = dummy.getFont().deriveFont(
				dummy.getFont().getSize() - 2f);

		// crawler
		JLabel lblCrawler = new JLabel("Crawler");
		lblCrawler.setFont(labelFont);
		lblCrawler.setForeground(Colors.VERY_STRONG_GRAY.getColor());
		getContentPane().add(lblCrawler, "cell 0 1,growx");

		EntityBrowsePanel<Crawler> crawlerBrowsePanel = new CrawlerBrowsePanel(
				servMan);
		crawlerBrowsePanel.setDeleteEntityEnabled(false);
		crawlerBrowsePanel.setCreateEntityEnabled(false);
		crawlerBrowsePanel.setEditEntityEnabled(false);
		crawlerBrowsePanel.setPreferredSize(new Dimension(486, 186));

		Getter<Crawler, String> searchBoxGetter = new Getter<Crawler, String>() {
			@Override
			public String get(Crawler crawler) {
				return servMan.getCrawlerService().getIdentifierForCrawler(
						crawler);
			}
		};

		SearchComboBox<Crawler> crawlerBox = new SearchComboBox<Crawler>(
				crawlerBrowsePanel, searchBoxGetter);
		crawlerBox.setEditable(true);
		crawlerBox.setValueRequired(true);
		crawlerBox.setValidationRule(new ValidationRule<Crawler>() {
			@Override
			public ValidationRuleResult evaluate(Crawler obj) {
				if (obj == null)
					return new ValidationRuleResult(false,
							"Please select a crawler.");
				else
					return ValidationRuleResult.getSimpleTrueInstance();
			}
		});

		FPJGUILabel<Crawler> categoryLabel = new FPJGUILabel<Crawler>();
		ToggleEditFormComponent<Crawler> crawlerToggle = new ToggleEditFormComponent<Crawler>(
				categoryLabel, crawlerBox);
		registerToggleComponent(crawlerToggle);
		getContentPane().add(crawlerToggle, "cell 0 2,growx");

		// request
		JLabel lblRequestString = new JLabel("Request String");
		lblRequestString.setFont(labelFont);
		lblRequestString.setForeground(Colors.VERY_STRONG_GRAY.getColor());
		getContentPane().add(lblRequestString, "cell 0 3,growx");

		FPJGUITextPane requestStringField = new FPJGUITextPane();
		requestStringField.setValidationRule(new ValidationRule<String>() {
			@Override
			public ValidationRuleResult evaluate(String arg0) {
				if (arg0 == null || arg0.isEmpty())
					return new ValidationRuleResult(false,
							"Please specify a request string (eg. q=\"e-health\").");
				else
					return ValidationRuleResult.getSimpleTrueInstance();
			}
		});
		FPJGUIMultilineLabel requestStringLabel = new FPJGUIMultilineLabel();
		ToggleEditFormComponent<String> requestStringToggle = new ToggleEditFormComponent<String>(
				requestStringLabel, requestStringField);
		registerToggleComponent(requestStringToggle);
		getContentPane().add(requestStringToggle, "cell 0 4,grow");

		// mappings
		ObjectGraphicalValueContainerMapper<AtomicRequestBuilder, Crawler> crawlerMapper = new ObjectGraphicalValueContainerMapper<AtomicRequestBuilder, Crawler>(
				crawlerToggle) {
			@Override
			public Crawler getValue(AtomicRequestBuilder object) {
				return object.getCrawler();
			}

			@Override
			public void setValue(AtomicRequestBuilder object, Crawler value) {
				object.setCrawler(value);
			}
		};
		getMappers().add(crawlerMapper);

		ObjectGraphicalValueContainerMapper<AtomicRequestBuilder, String> requestStringMapper = new ObjectGraphicalValueContainerMapper<AtomicRequestBuilder, String>(
				requestStringToggle) {
			@Override
			public String getValue(AtomicRequestBuilder object) {
				return object.getSearchString();
			}

			@Override
			public void setValue(AtomicRequestBuilder object, String value) {
				object.setSearchString(value);
			}
		};
		getMappers().add(requestStringMapper);

	}

}