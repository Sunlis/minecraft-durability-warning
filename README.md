minecraft-durability-warning
============================

Mod for Minecraft that warns you when your tools run low.

<h2>Usage</h2>

Requires a hook into one of Minecraft's classes that has a reference to the main Minecraft instance, like PlayerControllerMP. I cannot post Minecraft source code, so that part is up to you, but here's a clip to help you along:

```java
public class PlayerControllerMP
{
    ...
  
	public DurabilityWarning dw;
	
	...

    public PlayerControllerMP(Minecraft par1, NetClientHandler par2)
    {
		
        ...
		
		dw = new DurabilityWarning(par1);
    }
}

```

<h2>Why is this on Github?</h2>

I haven't been able to get MCP to work since 1.5, but I would like people to still be able to use the mod. If a helpful third party can make any necessary changes to make the mod 1.5-compatible, I will gladly accept your pull request.
