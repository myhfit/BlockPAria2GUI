package bp.ui.form;

import java.awt.Component;
import java.util.Map;

import javax.swing.SwingUtilities;

import bp.BPCore;
import bp.ui.scomp.BPTextField;
import bp.ui.scomp.BPTextFieldPane;
import bp.ui.util.CommonUIOperations;

public class BPFormPanelTaskAria2HTTP extends BPFormPanelTask
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8832343557495996383L;

	protected BPTextField m_txturl;
	protected BPTextField m_txtworkdir;
	protected BPTextField m_txtoutput;
	protected BPTextFieldPane m_panworkdir;

	public Map<String, Object> getFormData()
	{
		Map<String, Object> rc = super.getFormData();
		rc.put("url", m_txturl.getText());
		rc.put("workdir", m_txtworkdir.getText());
		rc.put("output", m_txtoutput.getText());
		return rc;
	}

	protected void initForm()
	{
		super.initForm();

		m_txturl = makeSingleLineTextField();

		m_panworkdir = makeSingleLineTextFieldPanel(this::onSelectWorkDir);
		m_txtworkdir = m_panworkdir.getTextComponent();

		m_txtoutput = makeSingleLineTextField();

		addLine(new String[] { "URL" }, new Component[] { m_txturl }, () -> !m_txturl.isEmpty());
		addLine(new String[] { "Work Dir" }, new Component[] { m_panworkdir });
		addLine(new String[] { "Output" }, new Component[] { m_txtoutput });
	}

	public void showData(Map<String, ?> data, boolean editable)
	{
		super.showData(data, editable);
		setComponentValue(m_txturl, data, "url", editable);
		setComponentValue(m_txtworkdir, data, "workdir", editable);
		setComponentValue(m_txtoutput, data, "output", editable);
	}

	protected String onSelectWorkDir(String oldpath)
	{
		String rc = CommonUIOperations.showOpenDirDialog(SwingUtilities.getWindowAncestor(this), oldpath);
		if (rc != null)
			rc = BPCore.getFileContext().comparePath(rc);
		return rc;
	}
}