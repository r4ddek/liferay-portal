/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.wiki.attachments.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcher;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.SearchContextTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.service.test.ServiceTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.wiki.model.WikiNode;
import com.liferay.wiki.model.WikiPage;
import com.liferay.wiki.service.WikiNodeLocalServiceUtil;
import com.liferay.wiki.service.WikiPageLocalServiceUtil;
import com.liferay.wiki.util.test.WikiTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Eudaldo Alonso
 * @author Roberto Díaz
 * @author Sergio González
 */
@RunWith(Arquillian.class)
public class WikiAttachmentsTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		ServiceTestUtil.setUser(TestPropsValues.getUser());

		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testDeleteAttachmentsWhenDeletingWikiNode() throws Exception {
		int initialFileEntriesCount =
			DLFileEntryLocalServiceUtil.getFileEntriesCount();

		addWikiPageAttachment();

		Assert.assertEquals(
			initialFileEntriesCount + 1,
			DLFileEntryLocalServiceUtil.getFileEntriesCount());

		WikiNodeLocalServiceUtil.deleteNode(_page.getNodeId());

		Assert.assertEquals(
			initialFileEntriesCount,
			DLFileEntryLocalServiceUtil.getFileEntriesCount());
	}

	@Test
	public void testDeleteAttachmentsWhenDeletingWikiPage() throws Exception {
		int initialFileEntriesCount =
			DLFileEntryLocalServiceUtil.getFileEntriesCount();

		addWikiPageAttachment();

		Assert.assertEquals(
			initialFileEntriesCount + 1,
			DLFileEntryLocalServiceUtil.getFileEntriesCount());

		WikiPageLocalServiceUtil.deletePage(
			_page.getNodeId(), _page.getTitle());

		Assert.assertEquals(
			initialFileEntriesCount,
			DLFileEntryLocalServiceUtil.getFileEntriesCount());
	}

	@Test
	public void testFoldersCountWhenAddingAttachmentsToSameWikiPage()
		throws Exception {

		int initialFoldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		addWikiPageAttachment();

		int foldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		Assert.assertEquals(initialFoldersCount + 3, foldersCount);

		addWikiPageAttachment();

		foldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		Assert.assertEquals(initialFoldersCount + 3, foldersCount);
	}

	@Test
	public void testFoldersCountWhenAddingWikiNode() throws Exception {
		int initialFoldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		addWikiNode();

		Assert.assertEquals(
			initialFoldersCount, DLFolderLocalServiceUtil.getDLFoldersCount());
	}

	@Test
	public void testFoldersCountWhenAddingWikiPage() throws Exception {
		int initialFoldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		addWikiPage();

		Assert.assertEquals(
			initialFoldersCount, DLFolderLocalServiceUtil.getDLFoldersCount());
	}

	@Test
	public void testFoldersCountWhenAddingWikiPageAttachment()
		throws Exception {

		int initialFoldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		addWikiPageAttachment();

		Assert.assertEquals(
			initialFoldersCount + 3,
			DLFolderLocalServiceUtil.getDLFoldersCount());
	}

	@Test
	public void testFoldersCountWhenAddingWikiPageAttachments()
		throws Exception {

		int foldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		addWikiPageAttachment();

		Assert.assertEquals(
			foldersCount + 3, DLFolderLocalServiceUtil.getDLFoldersCount());

		foldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		_page = null;

		addWikiPageAttachment();

		Assert.assertEquals(
			foldersCount + 1, DLFolderLocalServiceUtil.getDLFoldersCount());

		foldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		_node = null;
		_page = null;

		addWikiPageAttachment();

		Assert.assertEquals(
			foldersCount + 2, DLFolderLocalServiceUtil.getDLFoldersCount());

		foldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		Group group = _group;

		_group = null;
		_node = null;
		_page = null;

		try {
			addWikiPageAttachment();

			Assert.assertEquals(
				foldersCount + 3, DLFolderLocalServiceUtil.getDLFoldersCount());
		}
		finally {
			GroupLocalServiceUtil.deleteGroup(group);
		}
	}

	@Test
	public void testFoldersCountWhenDeletingWikiNodeWithAttachments()
		throws Exception {

		int initialFoldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		addWikiPageAttachment();

		Assert.assertEquals(
			initialFoldersCount + 3,
			DLFolderLocalServiceUtil.getDLFoldersCount());

		WikiNodeLocalServiceUtil.deleteNode(_page.getNodeId());

		Assert.assertEquals(
			initialFoldersCount + 1,
			DLFolderLocalServiceUtil.getDLFoldersCount());
	}

	@Test
	public void testFoldersCountWhenDeletingWikiNodeWithoutAttachments()
		throws Exception {

		int initialFoldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		addWikiNode();

		Assert.assertEquals(
			initialFoldersCount, DLFolderLocalServiceUtil.getDLFoldersCount());

		WikiNodeLocalServiceUtil.deleteNode(_node.getNodeId());

		Assert.assertEquals(
			initialFoldersCount, DLFolderLocalServiceUtil.getDLFoldersCount());
	}

	@Test
	public void testFoldersCountWhenDeletingWikiPageWithAttachments()
		throws Exception {

		int initialFoldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		addWikiPageAttachment();

		Assert.assertEquals(
			initialFoldersCount + 3,
			DLFolderLocalServiceUtil.getDLFoldersCount());

		WikiPageLocalServiceUtil.deletePage(
			_page.getNodeId(), _page.getTitle());

		Assert.assertEquals(
			initialFoldersCount + 2,
			DLFolderLocalServiceUtil.getDLFoldersCount());
	}

	@Test
	public void testFoldersCountWhenDeletingWikiPageWithoutAttachments()
		throws Exception {

		int initialFoldersCount = DLFolderLocalServiceUtil.getDLFoldersCount();

		addWikiPage();

		Assert.assertEquals(
			initialFoldersCount, DLFolderLocalServiceUtil.getDLFoldersCount());

		WikiPageLocalServiceUtil.deletePage(
			_page.getNodeId(), _page.getTitle());

		Assert.assertEquals(
			initialFoldersCount, DLFolderLocalServiceUtil.getDLFoldersCount());
	}

	@Test
	public void testMoveToTrashAndDeleteWikiPageAttachment() throws Exception {
		addWikiPage();

		_trashWikiAttachments(false);
	}

	@Test
	public void testMoveToTrashAndRestoreWikiPageAttachment() throws Exception {
		addWikiPage();

		_trashWikiAttachments(true);
	}

	@Test
	public void testSearchIncludeAttachment() throws Exception {
		String title = "title";

		_addFileEntry(title);
		_addWikiPageWithAttachmentFileName(title);

		Assert.assertEquals(1, _searchCount(title, false));
		Assert.assertEquals(2, _searchCount(title, true));
	}

	protected void addWikiNode() throws Exception {
		if (_group == null) {
			_group = GroupTestUtil.addGroup();
		}

		_node = WikiTestUtil.addNode(_group.getGroupId());
	}

	protected void addWikiPage() throws Exception {
		if (_node == null) {
			addWikiNode();
		}

		_page = WikiTestUtil.addPage(
			_group.getGroupId(), _node.getNodeId(), true);
	}

	protected void addWikiPageAttachment() throws Exception {
		if (_page == null) {
			addWikiPage();
		}

		WikiTestUtil.addWikiAttachment(
			_page.getUserId(), _page.getNodeId(), _page.getTitle(), getClass());
	}

	private void _addFileEntry(String title) throws PortalException {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(_group.getGroupId());

		_dlAppLocalService.addFileEntry(
			serviceContext.getUserId(), _group.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			StringUtil.randomString(), ContentTypes.TEXT_PLAIN, title,
			StringPool.BLANK, StringPool.BLANK, _CONTENT.getBytes(),
			serviceContext);
	}

	private void _addWikiPageWithAttachmentFileName(String fileName)
		throws Exception {

		if (_page == null) {
			addWikiPage();
		}

		WikiTestUtil.addWikiAttachment(
			_page.getUserId(), _page.getNodeId(), _page.getTitle(), fileName,
			getClass());
	}

	private int _searchCount(String keywords, boolean includeAttachments)
		throws Exception {

		SearchContext searchContext = SearchContextTestUtil.getSearchContext(
			_group.getGroupId());

		searchContext.setIncludeAttachments(includeAttachments);
		searchContext.setKeywords(keywords);

		FacetedSearcher facetedSearcher =
			_facetedSearcherManager.createFacetedSearcher();

		Hits hits = facetedSearcher.search(searchContext);

		return hits.getLength();
	}

	private void _trashWikiAttachments(boolean restore) throws Exception {
		int initialNotInTrashCount = _page.getAttachmentsFileEntriesCount();
		int initialTrashEntriesCount =
			_page.getDeletedAttachmentsFileEntriesCount();

		String fileName = RandomTestUtil.randomString() + ".docx";

		WikiTestUtil.addWikiAttachment(
			TestPropsValues.getUserId(), _node.getNodeId(), _page.getTitle(),
			fileName, getClass());

		Assert.assertEquals(
			initialNotInTrashCount + 1, _page.getAttachmentsFileEntriesCount());
		Assert.assertEquals(
			initialTrashEntriesCount,
			_page.getDeletedAttachmentsFileEntriesCount());

		FileEntry fileEntry =
			WikiPageLocalServiceUtil.movePageAttachmentToTrash(
				TestPropsValues.getUserId(), _page.getNodeId(),
				_page.getTitle(), fileName);

		Assert.assertEquals(
			initialNotInTrashCount, _page.getAttachmentsFileEntriesCount());
		Assert.assertEquals(
			initialTrashEntriesCount + 1,
			_page.getDeletedAttachmentsFileEntriesCount());

		if (restore) {
			WikiPageLocalServiceUtil.restorePageAttachmentFromTrash(
				TestPropsValues.getUserId(), _page.getNodeId(),
				_page.getTitle(), fileName);

			Assert.assertEquals(
				initialNotInTrashCount + 1,
				_page.getAttachmentsFileEntriesCount());
			Assert.assertEquals(
				initialTrashEntriesCount,
				_page.getDeletedAttachmentsFileEntriesCount());

			WikiPageLocalServiceUtil.deletePageAttachment(
				_page.getNodeId(), _page.getTitle(), fileName);
		}
		else {
			WikiPageLocalServiceUtil.deletePageAttachment(
				_page.getNodeId(), _page.getTitle(), fileEntry.getTitle());

			Assert.assertEquals(
				initialNotInTrashCount, _page.getAttachmentsFileEntriesCount());
			Assert.assertEquals(
				initialTrashEntriesCount,
				_page.getDeletedAttachmentsFileEntriesCount());
		}
	}

	private static final String _CONTENT =
		"Content: Enterprise. Open Source. For Life.";

	@Inject
	private static DLAppLocalService _dlAppLocalService;

	@Inject
	private static FacetedSearcherManager _facetedSearcherManager;

	@DeleteAfterTestRun
	private Group _group;

	private WikiNode _node;
	private WikiPage _page;

}