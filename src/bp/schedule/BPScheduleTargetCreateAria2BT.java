package bp.schedule;

import java.nio.file.StandardWatchEventKinds;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import bp.BPCore;
import bp.task.BPTask;
import bp.task.BPTaskFactory;
import bp.util.ClassUtil;

public class BPScheduleTargetCreateAria2BT implements BPScheduleTarget
{
	public void accept(Long t, BPScheduleTargetParams params)
	{
		String filename = (String) params.datas.get("context");
		String kind = params.datas.get("kind").toString();
		if (StandardWatchEventKinds.ENTRY_CREATE.name().equals(kind) && filename.endsWith(".torrent"))
		{
			addTask(filename);
		}
	}

	protected void addTask(String filename)
	{
		ServiceLoader<BPTaskFactory> facs = ClassUtil.getExtensionServices(BPTaskFactory.class);
		BPTaskFactory tfac = null;
		for (BPTaskFactory fac : facs)
		{
			String[] exts = fac.getExts();
			if (exts != null)
			{
				for (String e : exts)
				{
					if (e.equals(".torrent"))
					{
						tfac = fac;
						break;
					}
				}
			}
		}
		if (tfac != null)
		{
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("_CREATE_FROM_FILE", filename);
			BPTask<?> task = tfac.create(params);
			if (task != null)
			{
				BPCore.addTask(task);
			}
		}
	}

	public final static class BPScheduleTargetFactoryCA2BT implements BPScheduleTargetFactory
	{
		public String getName()
		{
			return "Create Task:Aria2BT";
		}

		public BPScheduleTarget create(Map<String, Object> params)
		{
			BPScheduleTarget rc = new BPScheduleTargetCreateAria2BT();
			return rc;
		}
	}
}
