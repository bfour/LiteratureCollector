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

package com.github.bfour.fpliteraturecollector.gui;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.github.bfour.fpjcommons.utils.Getter;
import com.github.bfour.fpjgui.abstraction.feedback.Feedback;
import com.github.bfour.fpjgui.abstraction.feedback.FeedbackListener;
import com.github.bfour.fpjgui.abstraction.feedback.FeedbackProvider;
import com.github.bfour.fpjgui.abstraction.feedback.FeedbackProviderProxy;
import com.github.bfour.fpjgui.components.PlainToolbar;
import com.github.bfour.fpjgui.layout.Orientation;
import com.github.bfour.fpjguiextended.tagging.TagsWindow;
import com.github.bfour.fpliteraturecollector.domain.Tag;
import com.github.bfour.fpliteraturecollector.domain.builders.TagBuilder;
import com.github.bfour.fpliteraturecollector.gui.authors.AuthorsWindow;
import com.github.bfour.fpliteraturecollector.gui.design.Icons;
import com.github.bfour.fpliteraturecollector.gui.literature.LiteratureWindow;
import com.github.bfour.fpliteraturecollector.gui.protocol.ProtocolWindow;
import com.github.bfour.fpliteraturecollector.service.ServiceManager;

public class MainPanel extends JPanel implements FeedbackProvider,
		FeedbackListener {

	private static final long serialVersionUID = 1144467669251836304L;
	private FeedbackProviderProxy feedbackProxy;
	private LiteratureWindow litWindow;

	/**
	 * Create the panel.
	 * 
	 * @param serviceManager
	 */
	public MainPanel(final ServiceManager serviceManager) {

		this.feedbackProxy = new FeedbackProviderProxy();

		setLayout(new MigLayout("insets 0", "[grow]", "[]20[grow]"));

		// toolbar
		PlainToolbar toolbar = new PlainToolbar(Orientation.CENTERED);

		// JButton createQueryButton = new JButton("Create query",
		// Icons.ADD.getIcon());
		// createQueryButton.setIconTextGap(6);
		// createQueryButton.setMargin(new Insets(4, 16, 4, 16));
		// toolbar.add(createQueryButton);

		JButton browseAuthorsButton = new JButton("Authors",
				Icons.PERSON_GROUP.getIcon());
		browseAuthorsButton.setIconTextGap(6);
		browseAuthorsButton.setMargin(new Insets(4, 16, 4, 16));
		toolbar.add(browseAuthorsButton);

		JButton browseLiteratureButton = new JButton("Literature",
				Icons.BOOKS.getIcon());
		browseLiteratureButton.setIconTextGap(6);
		browseLiteratureButton.setMargin(new Insets(4, 16, 4, 16));
		toolbar.add(browseLiteratureButton);

		JButton tagsButton = new JButton("Tags", Icons.TAG_32.getIcon());
		tagsButton.setIconTextGap(6);
		tagsButton.setMargin(new Insets(4, 16, 4, 16));
		toolbar.add(tagsButton);

		JButton duplicatesButton = new JButton("Duplicates",
				Icons.DUPLICATE.getIcon());
		duplicatesButton.setIconTextGap(6);
		duplicatesButton.setMargin(new Insets(4, 16, 4, 16));
		toolbar.add(duplicatesButton);

		JButton protocolButton = new JButton("Protocol",
				Icons.PROTOCOL.getIcon());
		protocolButton.setIconTextGap(6);
		protocolButton.setMargin(new Insets(4, 16, 4, 16));
		toolbar.add(protocolButton);

		add(toolbar, "cell 0 0, grow");

		// content
		final QueryOverviewPanel queryOverviewPanel = new QueryOverviewPanel(
				serviceManager);
		add(queryOverviewPanel, "cell 0 1,grow");
		queryOverviewPanel.addFeedbackListener(this);

		// logic
		// createQueryButton.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// queryOverviewPanel.createNew();
		// }
		// });

		browseAuthorsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AuthorsWindow.getInstance(serviceManager).setVisible(true);
			}
		});

		browseLiteratureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (litWindow == null)
					litWindow = new LiteratureWindow(serviceManager);
				litWindow.setVisible(true);
			}
		});

		tagsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new TagsWindow<Tag, TagBuilder>(Tag.class, serviceManager
						.getTagService(), new Getter<Tag, TagBuilder>() {
					@Override
					public TagBuilder get(Tag input) {
						if (input == null)
							return new TagBuilder();
						else
							return new TagBuilder(input);
					}
				}).setVisible(true);
			}
		});

		duplicatesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DuplicateWindow.getInstance(serviceManager).setVisible(true);
			}
		});

		protocolButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ProtocolWindow.getInstance(serviceManager).setVisible(true);
			}
		});

	}

	@Override
	public void addFeedbackListener(FeedbackListener listener) {
		feedbackProxy.addFeedbackListener(listener);
	}

	@Override
	public void removeFeedbackListener(FeedbackListener listener) {
		feedbackProxy.addFeedbackListener(listener);
	}

	@Override
	public void feedbackBroadcasted(Feedback arg0) {
		feedbackProxy.feedbackBroadcasted(arg0);
	}

	@Override
	public void feedbackChanged(Feedback arg0, Feedback arg1) {
		feedbackProxy.feedbackChanged(arg0, arg1);
	}

	@Override
	public void feedbackRevoked(Feedback arg0) {
		feedbackProxy.feedbackRevoked(arg0);
	}

}