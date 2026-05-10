package bp.ui.form;

import java.awt.Component;
import java.util.Map;

import javax.swing.SwingUtilities;

import bp.BPCore;
import bp.ui.scomp.BPTextField;
import bp.ui.scomp.BPTextFieldPane;
import bp.ui.util.CommonUIOperations;

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
		if (oldfile != null && oldfile.trim().length() == 0)
			oldfile = null;
		return CommonUIOperations.showOpenFileDialog(SwingUtilities.getWindowAncestor(this), oldfile);
	}

	protected String onSelectWorkDir(String oldpath)
	{
		String rc = CommonUIOperations.showOpenDirDialog(SwingUtilities.getWindowAncestor(this), oldpath);
		if (rc != null)
			rc = BPCore.getFileContext().comparePath(rc);
		return rc;
	}
}