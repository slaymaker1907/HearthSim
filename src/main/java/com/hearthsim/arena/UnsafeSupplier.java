package com.hearthsim.arena;

@FunctionalInterface
public interface UnsafeSupplier<OutputT>
{
    public OutputT get() throws Exception;
    
    public default OutputT getSafely()
    {
        try
        {
            return this.get();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
