package bp.ui.form;

import java.awt.Component;
import java.util.Map;

import bp.BPCore;
import bp.res.BPResource;
import bp.res.BPResourceFileSystem;
import bp.ui.dialog.BPDialogSelectResource2;
import bp.ui.dialog.BPDialogSelectResource2.SELECTTYPE;
import bp.ui.scomp.BPTextField;
import bp.ui.scomp.BPTextFieldPane;

public class BPFormPanelTaskAria2BT extends BPFormPanelTask
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8457927099638888973L;

	protected BPTextField m_txttorrent;
	protected BPTextFieldPane m_pantorrent;
	protected BPTextField m_txtworkdir;
	protected BPTextFieldPane m_panworkdir;

	public Map<String, Object> getFormData()
	{
		Map<String, Object> rc = super.getFormData();
		rc.put("torrentfile", m_txttorrent.getText());
		rc.put("workdir", m_txtworkdir.getText());
		return rc;
	}

	protected void initForm()
	{
		super.initForm();

		m_pantorrent = makeSingleLineTextFieldPanel(this::onSelectTorrentFile);
		m_txttorrent = m_pantorrent.getTextComponent();

		m_panworkdir = makeSingleLineTextFieldPanel(this::onSelectWorkDir);
		m_txtworkdir = m_panworkdir.getTextComponent();

		addLine(new String[] { "Torrent File" }, new Component[] { m_pantorrent }, () -> !m_txttorrent.isEmpty());
		addLine(new String[] { "Work Dir" }, new Component[] { m_panworkdir });
	}

	public void showData(Map<String, ?> data, boolean editable)
	{
		super.showData(data, editable);
		setComponentValue(m_txttorrent, data, "torrentfile", editable);
		setComponentValue(m_txtworkdir, data, "workdir", editable);
	}

	protected String onSelectTorrentFile(String oldfile)
	{
		String rc = null;
		BPDialogSelectResource2 dlg = new BPDialogSelectResource2();
		dlg.setSelectType(SELECTTYPE.FILE);
		dlg.showOpen();
		BPResource res = dlg.getSelectedResource();
		if (res != null)
		{
			BPResourceFileSystem fres = (BPResourceFileSystem) res;
			rc = BPCore.getFileContext().comparePath(fres.getFileFullName());
		}
		return rc;
	}

	protected String onSelectWorkDir(String oldpath)
	{
		String rc = null;
		BPDialogSelectResource2 dlg = new BPDialogSelectResource2();
		dlg.setSelectType(SELECTTYPE.DIR);
		dlg.showOpen();
		BPResource res = dlg.getSelectedResource();
		if (res != null)
		{
			BPResourceFileSystem fres = (BPResourceFileSystem) res;
			rc = BPCore.getFileContext().comparePath(fres.getFileFullName());
		}
		return rc;
	}
}