package bp.ui.form.dynamic.controller.task;

import bp.ui.dialog.BPDialogSelectResource.SELECTSCOPE;
import bp.ui.form.dynamic.BPFormContext;

public class BPFormControllerTaskAria2BT extends BPFormControllerTaskAria2
{
	public Object select(String key, Object old, BPFormContext context)
	{
		switch (key)
		{
			case "torrentfile":
				return onSelectResourceFile((String) old, SELECTSCOPE.COMPUTER);
		}
		return super.select(key, old, context);
	}
}
