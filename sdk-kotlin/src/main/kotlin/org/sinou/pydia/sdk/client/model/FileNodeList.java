package org.sinou.pydio.sdk.client.model;

import com.pydio.cells.api.ui.FileNode;

import java.util.List;

public class FileNodeList {
    public int page;
    public int pageCount;
    public int offset;
    public int nodeCount;
    public List<FileNode> list;
}
