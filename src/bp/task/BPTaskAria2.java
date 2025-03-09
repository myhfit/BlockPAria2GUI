package bp.task;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import bp.BPCore;
import bp.data.BPTransferInfo;
import bp.env.BPEnvExternalTools;
import bp.env.BPEnvManager;
import bp.res.BPResourceDirLocal;
import bp.util.NumberUtil;
import bp.util.ObjUtil;
import bp.util.ProcessUtil;
import bp.util.Std;
import bp.util.ThreadUtil;
import bp.util.ThreadUtil.ProcessThread;

public abstract class BPTaskAria2 extends BPTaskLocal<Boolean> implements BPTaskTransmission<Boolean>
{
	protected volatile PipedInputStream m_bis;
	protected volatile PipedOutputStream m_bos;
	protected AtomicBoolean m_outputstopflag = new AtomicBoolean();
	protected volatile boolean m_nameredirected = false;

	protected volatile ConcurrentHashMap<String, BPTransferInfo> m_transtatmap;

	public BPTaskAria2()
	{
		setCommonStatus(COMMON_STATUS_STOPPED);
	}

	public String getTaskName()
	{
		return "Aria2";
	}

	@SuppressWarnings("unchecked")
	protected void doStart()
	{
		String exepath = getExePath();
		Map<String, Object> ps = (Map<String, Object>) m_params;
		String workdir = (String) ps.get("workdir");
		String args = makeArgs();
		if (exepath == null)
		{
			setFailed(new RuntimeException("Need Set Environment:[Common]\\\"ARIA2C_PATH\\\" First"));
			return;
		}
		if (args == null)
		{
			return;
		}
		setStarted();
		triggerStatusChanged();

		String[] cmdarr = ProcessUtil.fixCommandArgs(exepath, args);
		Process p;
		try
		{
			m_transtatmap = new ConcurrentHashMap<String, BPTransferInfo>();
			m_outputstopflag.set(false);
			PipedOutputStream bos = new PipedOutputStream();
			PipedInputStream bis = new PipedInputStream(bos);
			m_bos = bos;
			m_bis = bis;
			ThreadUtil.runNewThread(this::onOutput, true);
			p = new ProcessBuilder(cmdarr).directory(((BPResourceDirLocal) BPCore.getFileContext().getDir((workdir == null ? "." : workdir))).getFileObject()).redirectErrorStream(true).start();
			ProcessThread t = new ProcessThread(p);
			t.setOutputCollector((bs, len) ->
			{
				try
				{
					bos.write(bs, 0, len);
					bos.flush();
				}
				catch (IOException e)
				{
				}
			});
			t.start();

			ThreadUtil.doProcessLoop(p, t, () -> m_stopflag, (stopflag, exitcode) ->
			{
				if (stopflag)
				{
					setCompleted();
				}
				else
				{
					if (exitcode != 0)
					{
						setFailed(new RuntimeException("Exit Code:" + exitcode));
						BPCore.saveTasks();
						m_future.complete(false);
					}
					else
					{
						setCompleted();
						BPCore.saveTasks();
						m_future.complete(true);
					}
				}
			});
		}
		catch (IOException e)
		{
			if (m_stopflag)
			{
			}
			else
			{
				Std.err(e);
				setFailed(e);
				BPCore.saveTasks();
				m_future.completeExceptionally(e);
			}
		}
		finally
		{
			m_outputstopflag.set(true);
		}
	}

	protected String getCompleteText()
	{
		String rc = null;
		Map<String, BPTransferInfo> transtatmap = m_transtatmap;
		if (transtatmap != null)
		{
			BPTransferInfo allinfo = transtatmap.get("");
			if (allinfo != null)
			{
				if (allinfo.size > 0 && !m_stopflag)
				{
					allinfo.cur = allinfo.size;
					rc = formatAllInfo(allinfo, false);
				}
			}
		}
		return rc;
	}

	protected abstract LineAnalyzer createLineAnalyzer();

	protected String formatAllInfo(BPTransferInfo allinfo, boolean isrunning)
	{
//		Std.debug(allinfo.toString() + ":" + allinfo.cur + "/" + allinfo.size);
		long speed = allinfo.speed;
		long all = allinfo.size;
		long cur = allinfo.cur;
		StringBuilder sb = new StringBuilder();
		sb.append(NumberUtil.formatByteCount(cur));
		sb.append("/");
		sb.append(NumberUtil.formatByteCount(all));
		if (isrunning)
		{
			sb.append("  ");
			sb.append(NumberUtil.formatByteCount(speed));
			sb.append("/s");
		}
		if (allinfo.size > 0 && allinfo.cur >= 0)
		{
			float p = (float) ((double) allinfo.cur / (double) allinfo.size);
			if (p < 0f)
				p = 0f;
			if (p > 1f)
				p = 1f;
			sb.append("  ");
			sb.append(NumberUtil.formatPercent(p));
		}
		return sb.toString();
	}

	protected float getTransferProgress(BPTransferInfo info)
	{
		if (info.size > 0 && info.cur >= 0)
		{
			float p = (float) ((double) info.cur / (double) info.size);
			if (p < 0f)
				p = 0f;
			if (p > 1f)
				p = 1f;
			return p;
		}
		else if (info.fulltransfered)
		{
			return 1f;
		}
		return -1f;
	}

	protected void onOutput()
	{
		PipedInputStream bis = m_bis;
		try (InputStreamReader isr = new InputStreamReader(bis); BufferedReader reader = new BufferedReader(isr))
		{
			boolean flag;
			boolean stopflag;
			LinkedList<String> lines = new LinkedList<String>();
			LineAnalyzer analyzer = createLineAnalyzer();
			while (true)
			{
				flag = false;
				stopflag = m_outputstopflag.get();
				while (bis.available() > 0)
				{
					flag = true;
					String line = reader.readLine();
					lines.add(line);
					int c = lines.size();
					while (c > 10000)
					{
						lines.remove();
						c--;
					}
					if (analyzer.analyze(lines))
					{
						BPTransferInfo allinfo = m_transtatmap.get("");
						setProgressText(formatAllInfo(allinfo, true));
						float p = getTransferProgress(allinfo);
						if (p > -1f)
							setProgress(p);
						triggerStatusChanged();
					}
				}
				if (stopflag)
					break;
				if (!flag)
				{
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
		}
		catch (EOFException e)
		{
		}
		catch (IOException e)
		{
			Std.err(e);
		}
		finally
		{
			try
			{
				m_bis.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			try
			{
				m_bos.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	protected interface LineAnalyzer
	{
		boolean analyze(LinkedList<String> lines);
	}

	protected abstract String makeArgs();

	@SuppressWarnings("unchecked")
	public Map<String, Object> getMappedData()
	{
		Map<String, Object> rc = super.getMappedData();
		rc.putAll((Map<String, ?>) m_params);
		if (isCompleted())
		{
			rc.put("finished", true);
			rc.put("finishedtext", m_progresstext);
		}
		return rc;
	}

	public void setMappedData(Map<String, Object> data)
	{
		super.setMappedData(data);
		String finished = ObjUtil.toString(data.remove("finished"));
		String finishedtext = null;
		if (finished != null && "true".equals(finished))
		{
			finishedtext = (String) data.remove("finishedtext");
			setCompleted(finishedtext);
		}
		m_params = new ConcurrentHashMap<String, Object>(data);
	}

	protected final static String getExePath()
	{
		String exe = BPEnvManager.getEnvValue(BPEnvExternalTools.ENV_NAME_EXTERNALTOOLS, "ARIA2C_PATH");
		if (exe != null && exe.trim().length() == 0)
			exe = null;
		return exe;
	}

	protected final static boolean checkEnv()
	{
		String exe = BPEnvManager.getEnvValue(BPEnvExternalTools.ENV_NAME_EXTERNALTOOLS, "ARIA2C_PATH");
		if (exe != null && exe.length() > 0)
			return true;
		else
			return false;
	}
}
