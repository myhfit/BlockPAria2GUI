package bp.task;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import bp.BPCore;
import bp.data.BPTransferInfo;
import bp.env.BPEnvManager;
import bp.env.BPEnvTransmission;
import bp.res.BPResourceDir;
import bp.task.BPTaskFactory.BPTaskFactoryBase;

public class BPTaskAria2Magnet extends BPTaskAria2
{
	@SuppressWarnings("unchecked")
	protected String makeArgs()
	{
		Map<String, Object> params = (Map<String, Object>) m_params;
		String workdir = (String) params.get("workdir");
		StringBuilder sb = new StringBuilder();
		sb.append("--summary-interval=1");
		if (workdir != null && workdir.trim().length() > 0)
		{
			BPResourceDir dir = BPCore.getFileContext().getDir(workdir);
			if (dir.exists())
			{
				sb.append(" -d ");
				sb.append("\"");
				sb.append(dir.getFileFullName());
				sb.append("\"");
			}
		}
		sb.append(" \"");
		sb.append((String) params.get("magnet"));
		sb.append("\"");
		return sb.toString();
	}

	public String getTaskName()
	{
		return "Aria2 Magnet";
	}

	protected LineAnalyzer createLineAnalyzer()
	{
		return new LineAnalyzerAria2Magnet(m_transtatmap);
	}

	public static class BPTaskFactoryAria2Magnet extends BPTaskFactoryBase<BPTaskAria2Magnet> implements BPTaskFactoryTransmission
	{
		public String getName()
		{
			return "Aria2 Magnet";
		}

		protected BPTaskAria2Magnet createTask()
		{
			return new BPTaskAria2Magnet();
		}

		public BPTask<?> create(Map<String, Object> taskdata)
		{
			String text = (String) taskdata.get("_CREATE_FROM_TEXT");
			if (text != null)
			{
				taskdata.remove("_CREATE_FROM_TEXT");
				taskdata.put("magnet", text);
				taskdata.put("name", getMagnetName(text));
				String dlpath = BPEnvManager.getEnvValue(BPEnvTransmission.ENV_NAME_TRANSMISSION, BPEnvTransmission.ENVKEY_WORKDIR);
				if (dlpath != null && dlpath.length() > 0)
					taskdata.put("workdir", dlpath);
			}
			return super.create(taskdata);
		}

		public Class<? extends BPTask<?>> getTaskClass()
		{
			return BPTaskAria2Magnet.class;
		}

		public String[] getExts()
		{
			return new String[] { "[Magnet URI]" };
		}
	}

	protected final static String getMagnetName(String text)
	{
		String rc = null;
		String t = text;
		if (t.startsWith("magnet:"))
		{
			t = t.substring(7);
			int vi = t.indexOf("?");
			t = t.substring(vi + 1);
			String[] args = t.split("&");
			for (String arg : args)
			{
				vi = arg.indexOf("=");
				String key, value;
				if (vi != -1)
				{
					key = arg.substring(0, vi);
					value = arg.substring(vi + 1);
				}
				else
				{
					key = arg;
					value = null;
				}
				if (key.equals("xt"))
				{
					if (value.startsWith("urn:btih:"))
						rc = value.substring(9);
					break;
				}
			}
		}
		return rc;
	}

	public static class LineAnalyzerAria2Magnet implements LineAnalyzer
	{
		protected boolean m_issummary = false;
		protected Map<String, BPTransferInfo> m_transtatmap;
		protected BPTransferInfo m_allinfo;

		public LineAnalyzerAria2Magnet(Map<String, BPTransferInfo> transtatmap)
		{
			m_transtatmap = transtatmap;
			BPTransferInfo allinfo = new BPTransferInfo();
			allinfo.name = "all";
			m_allinfo = allinfo;
			transtatmap.put("", allinfo);
		}

		protected boolean checkSummaryStart(String line)
		{
			int c = line.length();
			for (int i = 0; i < c; i++)
			{
				if (line.charAt(i) != '=')
					return false;
			}
			return true;
		}

		protected void fillProgress(String seg, BPTransferInfo info, BPTransferInfo allinfo)
		{
			int vi = seg.indexOf("/");
			long cur = -1;
			long size = -1;
			if (vi > -1)
			{
				cur = calcNum(seg.substring(0, vi));
				String rstr = seg.substring(vi + 1);
				int vi2 = rstr.indexOf("(");
				if (vi2 > -1)
					size = calcNum(rstr.substring(0, vi2));
				else
					size = calcNum(rstr);
			}
			else
			{
				cur = calcNum(seg);
			}
			if (info != allinfo)
			{
				info.cur = cur;
				info.size = size;
			}
			if (cur >= 0)
				allinfo.cur += cur;
			if (size >= 0)
				allinfo.size += size;
		}

		protected void fillOtherInfo(String seg, BPTransferInfo info, BPTransferInfo allinfo)
		{
			if (seg.startsWith("DL:"))
			{
				long speed = calcNum(seg.substring(3).trim());
				allinfo.speed = speed;
			}
		}

		protected long calcNum(String numstr)
		{
			String nstr = null;
			String unit = null;
			int c = numstr.length();
			for (int i = 0; i < c; i++)
			{
				char ch = numstr.charAt(i);
				if (ch != '.' && (ch < '0' || ch > '9'))
				{
					nstr = numstr.substring(0, i);
					unit = numstr.substring(i);
					break;
				}
			}
			if (nstr == null)
				return -1;
			long rc = -1;
			double n = -1;
			try
			{
				n = Double.parseDouble(nstr);
				if ("KiB".equalsIgnoreCase(unit))
				{
					n *= 1024d;
				}
				else if ("MiB".equalsIgnoreCase(unit))
				{
					n *= 1048576d;
				}
				else if ("GiB".equalsIgnoreCase(unit))
				{
					n *= (double) 0x40000000;
				}
				else if ("TiB".equalsIgnoreCase(unit))
				{
					n *= (double) 0x40000000;
				}
				rc = Math.round(n);
			}
			catch (NumberFormatException e)
			{
				rc = -1;
			}
			return rc;
		}

		public boolean analyze(LinkedList<String> lines)
		{
			String line0 = null;
			Iterator<String> it = lines.descendingIterator();
			if (it.hasNext())
				line0 = it.next();
			if (line0 != null)
			{
				if (m_issummary)
				{
					BPTransferInfo allinfo = m_allinfo;
					if (line0.isEmpty())
					{
						m_issummary = false;
						lines.clear();
						m_transtatmap.put("", allinfo);
						return true;
					}
					if (line0.startsWith("FILE:"))
					{
						String filename = line0.substring(5).trim();
						String line1 = it.hasNext() ? it.next() : null;
						if (line1 != null)
						{
							if (line1.startsWith("[") && line1.endsWith("]"))
							{
								String[] ps = line1.substring(0, line1.length() - 1).split(" ");
								if (ps.length > 1)
								{
									String ps1 = ps[1];
									if (ps1.startsWith("SEED("))
									{
										if (allinfo.size > 0)
											allinfo.cur = allinfo.size;
										allinfo.fulltransfered = true;
									}
									else
									{
										allinfo.cur = 0;
										allinfo.size = 0;
										BPTransferInfo info = m_transtatmap.get(filename);
										if (info == null)
										{
											info = new BPTransferInfo();
											info.name = filename;
											m_transtatmap.put(filename, info);
										}
										fillProgress(ps[1], info, allinfo);
										for (int i = 2; i < ps.length; i++)
										{
											fillOtherInfo(ps[i], info, allinfo);
										}
									}
								}
								// Std.debug(ps[1] + " " + ps[2] + " " + ps[3]);
							}
						}
					}
				}
				else
				{
					if (line0.startsWith("====") && checkSummaryStart(line0))
					{
						m_issummary = true;
						lines.clear();
					}
				}
			}
			return false;
		}
	}
}
