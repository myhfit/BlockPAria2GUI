package bp.ext;

import bp.context.BPFileContext;
import bp.env.BPEnvExternalTools;
import bp.env.BPEnvManager;

public class BPExtensionLoaderAria2GUI implements BPExtensionLoaderGUI<Object>
{
	public String getName()
	{
		return "GUI for Aria2";
	}

	public String[] getParentExts()
	{
		return new String[] { "GUI-Swing" };
	}

	public String[] getDependencies()
	{
		return null;
	}

	public void preload()
	{
	}

	public String getUIType()
	{
		return "Swing";
	}

	public void install(BPFileContext context)
	{
		BPEnvExternalTools env = (BPEnvExternalTools) BPEnvManager.getEnv(BPEnvExternalTools.ENV_NAME_EXTERNALTOOLS);
		env.addRawKey("ARIA2C_PATH");
	}
}
