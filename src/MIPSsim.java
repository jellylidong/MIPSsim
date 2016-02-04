import java.io.*;

public class MIPSsim 
{	
	public static void main(String[] args) throws IOException
	{
		RGF[] reg = new RGF[8]; InitialDef(reg); SetInitVal(reg);	
		DAM[] mem = new DAM[64]; InitialDef(mem); SetInitVal(mem);
		
		MipsBufferLong INB = new MipsBufferLong(), LIB= new MipsBufferLong(), AIB= new MipsBufferLong(); 
		MipsBufferShort[] REB = new MipsBufferShort[2]; REB[0] = new MipsBufferShort();REB[1] = new MipsBufferShort();
		MipsBufferShort	ADB = new MipsBufferShort();
		
		INST INMhead = new INST() ,INMrear , INMtail= new INST() ; //use FIFO to implement INM
		INMrear = INMhead;
		
		int StepCounter = 0;
		
		FileInputStream fi = new FileInputStream("instructions.txt");
        InputStreamReader fs = new InputStreamReader(fi);
        BufferedReader fb = new BufferedReader(fs);
        String ch = "";// = fb.readLine();
        PrintWriter res = new PrintWriter(new FileWriter("simulation.txt"));
        /*********************FIRT TIME BEGIN*********************************/
        for(int i = 0; i<INMSize; i++)
        {        	
        	ch = fb.readLine();
        	assign_inst_val(ch, INMrear);
        	INMtail = INMrear;       	
        	if(i < INMSize-1)
			{
        		INMrear.next = new INST();
        		INMrear = INMrear.next;        		
			}        	        	
        }
        PrintRes(INMhead, INB, AIB, LIB, ADB, REB, reg, mem, StepCounter,res);
        /*********************FIRT TIME END*********************************/
        while(INMhead.inst.operand != "" || REB[0].val != pseudo_init_val)
		{
			StepCounter++;
			/*********WRITE************/
			if(REB[0].val != pseudo_init_val)
			{
				if(REB[1].val != pseudo_init_val)
				{
						reg[Integer.parseInt(REB[0].address.substring(1))].val = REB[0].val;
						REB[0] = REB[1];
						REB[1] = new MipsBufferShort();
				}
				else
				{
					reg[Integer.parseInt(REB[0].address.substring(1))].val = REB[0].val;
					REB[0] = new MipsBufferShort();
				}
			}
			/**********LOAD************/
			if(ADB.val != pseudo_init_val)
			{
				REB[0].address = ADB.address;
				REB[0].val = mem[ADB.val%64].val;
				ADB = new MipsBufferShort();
			}			
			/***********ASU************/
			if(AIB.operand.equals("ADD"))
			{
				if(REB[0].val != pseudo_init_val)
				{
					REB[1].val = Integer.parseInt(AIB.src1) + Integer.parseInt(AIB.src2);
					REB[1].address = AIB.dest;
				}
				else
				{
					REB[0].val = Integer.parseInt(AIB.src1) + Integer.parseInt(AIB.src2);
					REB[0].address = AIB.dest;
				}
				AIB = new MipsBufferLong();
			}
			else if(AIB.operand.equals("SUB")) 
			{
				if(REB[0].val != pseudo_init_val)
				{
					REB[1].val = Integer.parseInt(AIB.src1) - Integer.parseInt(AIB.src2);
					REB[1].address = AIB.dest;
				}
				else
				{
					REB[0].val = Integer.parseInt(AIB.src1) - Integer.parseInt(AIB.src2);
					REB[0].address = AIB.dest;
				}
				AIB = new MipsBufferLong();
			}
			/***********ADDR************/
			else
			{
				ADB.address = LIB.dest;
				if(LIB.operand.equals("LD"))
				{
					ADB.val = Integer.parseInt(LIB.src1) + Integer.parseInt(LIB.src2);
					LIB = new MipsBufferLong(); //maybe use a function like clear buffer can be better
				}
			}			
			/***********ISSUE************/
			if(INB.operand.equals("ADD") || INB.operand.equals("SUB"))	
			{
				AIB = INB;	
				LIB = new MipsBufferLong(); //System.out.println("LIBAAA "+LIB.operand);
			}
			else if(INB.operand.equals("LD")) 
			{
				LIB = INB;	
				AIB = new MipsBufferLong();
			}			
			/*************ID************/
			INB = INMhead.inst;
			READ(INMhead, INB, reg);
			INMhead = INMhead.next;
			/*************IF************/
			INMtail.next = new INST();
			INMtail = INMtail.next;//
			ch = fb.readLine();
			assign_inst_val(ch, INMtail);
			PrintRes(INMhead, INB, AIB, LIB, ADB, REB, reg, mem, StepCounter,res);
		}
		res.close();	fb.close();
	}
	static final int pseudo_init_val = -2147483647;
	static final int INMSize = 8;
	public static class MipsBufferLong
	{
		String operand = "" , dest = "" , src1 = "" , src2 = "" ;
		String status = "" ; 
	}
	public static class MipsBufferShort
	{
		String address = "" ;
		int    val 	   = pseudo_init_val;
	}
	public static class INST
	{
		MipsBufferLong inst = new MipsBufferLong();
		INST next; 
	}
	public static class RGF
	{
		String address = "" ;
		int    val 	   = pseudo_init_val;
	}
	public static class DAM
	{
		String address = "" ;
		int    val 	   = pseudo_init_val;
	}
	public static void InitialDef(MipsBufferLong[] a)
	{
		for(int i = 0; i < a.length; i++)
			a[i] = new MipsBufferLong();
	}
	public static void InitialDef(MipsBufferShort[] a)
	{
		for(int i = 0; i < a.length; i++)
			a[i] = new MipsBufferShort();
	}
	public static void InitialDef(RGF[] a)
	{
		for(int i = 0; i < a.length; i++)
		{	a[i] = new RGF();	a[i].address = "R"+Integer.toString(i);}
	}
	public static void InitialDef(DAM[] a)
	{
		for(int i = 0; i < a.length; i++)
		{	a[i] = new DAM();	a[i].address = Integer.toString(i);}
	}
	public static void SetInitVal(DAM[] a) throws IOException
	{
		FileInputStream fi = new FileInputStream("datamemory.txt");
        InputStreamReader fs = new InputStreamReader(fi);
        BufferedReader fb = new BufferedReader(fs);
        String ch = fb.readLine();
        while(ch != null)
    	{
    		int comma_pos = ch.indexOf(",");
    		int end_pos   = ch.indexOf(">");
    		int begin_pos = ch.indexOf("<");
        	int addr = Integer.parseInt(ch.substring(begin_pos+1,comma_pos));
        	int val = Integer.parseInt(ch.substring(comma_pos+1, end_pos));
    		a[addr].val = val;
    		a[addr].address = ch.substring(begin_pos+1, comma_pos);
    		ch = fb.readLine();
    	}
        fb.close();
	}
	public static void SetInitVal(RGF[] a) throws IOException
	{
		FileInputStream fi = new FileInputStream("registers.txt");
        InputStreamReader fs = new InputStreamReader(fi);
        BufferedReader fb = new BufferedReader(fs);
        String ch = fb.readLine();
        while(ch != null)
    	{
    		int comma_pos = ch.indexOf(",");
    		int end_pos   = ch.indexOf(">");
    		int begin_pos = ch.indexOf("<");
        	int addr = Integer.parseInt(ch.substring(begin_pos+2,comma_pos));
        	int val = Integer.parseInt(ch.substring(comma_pos+1, end_pos));
    		a[addr].val = val;
    		a[addr].address = ch.substring(begin_pos+1, comma_pos);
    		ch = fb.readLine();
    	}
        fb.close();
	}		
	public static void READ(INST current_inst, MipsBufferLong INB, RGF[] reg )
	{
		if(current_inst.inst.operand != "")	
		{	
			if (current_inst.inst.src1.charAt(0) == 'R')	
				INB.src1 = Integer.toString(reg[Integer.parseInt(current_inst.inst.src1.substring(1,2))].val);
			if (current_inst.inst.src2.charAt(0) == 'R')	
				INB.src2 = Integer.toString(reg[Integer.parseInt(current_inst.inst.src2.substring(1,2))].val);
		}
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
	public static void PrintRes(INST head, MipsBufferLong INB, MipsBufferLong AIB, MipsBufferLong LIB,
			MipsBufferShort ADB, MipsBufferShort[] REB, RGF[] reg, DAM[] mem, int StepCounter, PrintWriter res)
	{
		res.print("STEP "+StepCounter+":"+"\r\n");
		INST rear = head;//new INST();
		//rear = head;
		res.print("INM:");
		int pos_null = INMSize;
		for(int i = 0; (i<INMSize) && (pos_null == INMSize); i++)
		{
			if(rear.inst.operand.equals("")) pos_null = i;
			if(i <INMSize-1 )rear = rear.next;
		}
		rear = head;
		//res.print(pos_null);
		if(pos_null!= 0)
		{
			for(int i = 0; i < pos_null; i++) //i < INMSize && !rear.inst.operand.equals("")
			{
				//if(!rear.next.inst.dest.equals(""))//modified && !rear.next.equals(null)
				if(i < pos_null - 1 )
					 res.print("<"+rear.inst.operand+","+rear.inst.dest+","+rear.inst.src1+","+rear.inst.src2+">"+",");
				else 
					res.print("<"+rear.inst.operand+","+rear.inst.dest+","+rear.inst.src1+","+rear.inst.src2+">"+"\r\n");
				//else res.println();
				if(i <INMSize-1 )rear = rear.next;
			}
		}
		else res.print("\r\n");
		
		res.print("INB:");
		if(INB.operand != "")
			res.print("<"+INB.operand+","+INB.dest+","+INB.src1+","+INB.src2+">"+"\r\n");
		else res.print("\r\n");
		
		res.print("AIB:");
		if(AIB.operand != "")
			res.print("<"+AIB.operand+","+AIB.dest+","+AIB.src1+","+AIB.src2+">"+"\r\n");
		else res.print("\r\n");
		
		res.print("LIB:");		
		if(LIB.operand != "")
			res.print("<"+LIB.operand+","+LIB.dest+","+LIB.src1+","+LIB.src2+">"+"\r\n");
		else res.print("\r\n");
		
		res.print("ADB:");
		if(ADB.val != pseudo_init_val)
			res.print("<"+ADB.address+","+ADB.val+">"+"\r\n");
		else res.print("\r\n");
		
		res.print("REB:");
		if(REB[1].val != pseudo_init_val)
			res.print("<"+REB[0].address+","+REB[0].val+">"+","+"<"+REB[1].address+","+REB[1].val+">"+"\r\n");
		else if(REB[0].val != pseudo_init_val)
			res.print("<"+REB[0].address+","+REB[0].val+">"+"\r\n");
		else res.print("\r\n");
		
		int index1[] = new int[8]; int j1 = 0;
		for(int i = 0; i< index1.length; i++) index1[i] = -1;
		res.print("RGF:");
		for(int i = 0; i < reg.length; i++)
		{
			if(reg[i].val != pseudo_init_val) index1[j1++] = i;
		}
		for(int i = 0; i < j1; i++)
		{
			if(i<j1-1) res.print("<"+reg[index1[i]].address+","+reg[index1[i]].val+">"+",");
			else 	  res.print("<"+reg[index1[i]].address+","+reg[index1[i]].val+">"+"\r\n");
		}
		
		int index2[] = new int[64]; int j2 = 0;
		for(int i = 0; i< index1.length; i++) index2[i] = -1;
		res.print("DAM:");
		for(int i = 0; i < mem.length; i++)
		{
			if(mem[i%64].val != pseudo_init_val) index2[j2++] = i;
		}
		for(int i = 0; i < j2; i++)
		{
			if(i<j2-1) res.print("<"+mem[index2[i]%64].address+","+mem[index2[i]%64].val+">"+",");
			else if(head.inst.operand != "" || REB[0].val != pseudo_init_val)
				res.print("<"+mem[index2[i]%64].address+","+mem[index2[i]%64].val+">"+"\r\n");
			else res.print("<"+mem[index2[i]%64].address+","+mem[index2[i]%64].val+">");
		}
		if(head.inst.operand != "" || REB[0].val != pseudo_init_val)
			res.print("\r\n");
	}
}
