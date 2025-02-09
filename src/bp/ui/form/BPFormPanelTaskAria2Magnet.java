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

public class BPFormPanelTaskAria2Magnet extends BPFormPanelTask
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5914212021690342607L;
	protected BPTextField m_txtmagnet;
	protected BPTextField m_txtworkdir;
	protected BPTextFieldPane m_panworkdir;

	public Map<String, Object> getFormData()
	{
		Map<String, Object> rc = super.getFormData();
		rc.put("magnet", m_txtmagnet.getText());
		rc.put("workdir", m_txtworkdir.getText());
		return rc;
	}

	protected void initForm()
	{
		super.initForm();

		m_txtmagnet = makeSingleLineTextField();

		m_panworkdir = makeSingleLineTextFieldPanel(this::onSelectWorkDir);
		m_txtworkdir = m_panworkdir.getTextComponent();

		addLine(new String[] { "Magnet" }, new Component[] { m_txtmagnet }, () -> !m_txtmagnet.isEmpty());
		addLine(new String[] { "Work Dir" }, new Component[] { m_panworkdir });
	}

	public void showData(Map<String, ?> data, boolean editable)
	{
		super.showData(data, editable);
		setComponentValue(m_txtmagnet, data, "magnet", editable);
		setComponentValue(m_txtworkdir, data, "workdir", editable);
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