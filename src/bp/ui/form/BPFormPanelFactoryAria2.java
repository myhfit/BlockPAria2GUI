package bp.ui.form;

import java.util.function.BiConsumer;

import bp.task.BPTaskAria2BT;
import bp.task.BPTaskAria2HTTP;
import bp.task.BPTaskAria2Magnet;

public class BPFormPanelFactoryAria2 implements BPFormPanelFactory
{
	public void register(BiConsumer<String, Class<? extends BPFormPanel>> regfunc)
	{
		regfunc.accept(BPTaskAria2HTTP.class.getName(), BPFormPanelTaskAria2HTTP.class);
		regfunc.accept(BPTaskAria2BT.class.getName(), BPFormPanelTaskAria2BT.class);
		regfunc.accept(BPTaskAria2Magnet.class.getName(), BPFormPanelTaskAria2Magnet.class);
	}
}
