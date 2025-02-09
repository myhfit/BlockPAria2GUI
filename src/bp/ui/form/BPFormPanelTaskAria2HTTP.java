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