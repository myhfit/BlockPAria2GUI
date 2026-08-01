package bp.ui.form.dynamic.controller.task;

import bp.ui.dialog.BPDialogSelectResource.SELECTSCOPE;
import bp.ui.form.dynamic.BPFormContext;

public class BPFormControllerTaskAria2 extends BPFormControllerTask
{
	public Object select(String key, Object old, BPFormContext context)
	{
		switch (key)
		{
			case "workdir":
				return onSelectResourceDir((String) old, SELECTSCOPE.COMPUTER);
		}
		return super.select(key, old, context);
	}
}
