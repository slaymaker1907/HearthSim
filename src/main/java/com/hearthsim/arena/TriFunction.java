package com.hearthsim.arena;

@FunctionalInterface
public interface TriFunction <InputT1, InputT2, InputT3, OutputT> 
{
    public OutputT apply(InputT1 input1, InputT2 input2, InputT3 input3) throws Exception;
}
