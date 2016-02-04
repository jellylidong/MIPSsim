INST head= new INST(),rear= new INST(),tail= new INST();

public static class INST
	{
		MipsBufferLong inst;
		INST next; 
	}

public static int Sizeof(INST head, INST tail)
{
	int i = 0;
	INST rear = new INST();
	rear = head;
	while( rear != tial)
	{
		i++;
		rear =rear.next
	}
	return i+1;
}

public static void IF(INST head, INST tail,BufferedReader fb)
{
	tail.next = new INST();
	tail = tail.next;
	String ch = fb.readLine(); 
	assign_inst_val(ch, tail);	
}

public static void assign_inst_val(String ch, INST tail)
{
	if(ch != null)
    	{
		int[] sym_pos = new int[4];
    		sym_pos[3] = ch.length()-1;
    		int index = 0;
    		for(int i = 0; i < ch.length()-2; i++)
    		{
    			if( ch.substring(i,i+1).equals(",") )
    			{	
    				sym_pos[index] = i; 
    				if(index < sym_pos.length) 
    					index++;
    			}
    		}
    		tail.inst.operand = ch.substring(1, sym_pos[0]);
    		tail.inst.dest = ch.substring(sym_pos[0]+1, sym_pos[1]);
    		tail.inst.src1 = ch.substring(sym_pos[1]+1, sym_pos[2]);
    		tail.inst.src2 = ch.substring(sym_pos[2]+1, sym_pos[3]);
	}
	else
	{
		tail.inst.operand = "";
    		tail.inst.dest = "";
    		tail.inst.src1 = "";
    		tail.inst.src2 = "";
	}
}
