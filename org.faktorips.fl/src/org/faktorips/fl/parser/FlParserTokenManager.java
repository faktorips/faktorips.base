/* Generated By:JJTree&JavaCC: Do not edit this line. FlParserTokenManager.java */
package org.faktorips.fl.parser;

public class FlParserTokenManager implements FlParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x10000000L) != 0L)
            return 41;
         if ((active0 & 0x6000000L) != 0L)
            return 61;
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 33:
         jjmatchedKind = 29;
         return jjMoveStringLiteralDfa1_0(0x100000L);
      case 40:
         return jjStopAtPos(0, 30);
      case 41:
         return jjStopAtPos(0, 31);
      case 42:
         return jjStopAtPos(0, 27);
      case 43:
         return jjStartNfaWithStates_0(0, 25, 61);
      case 45:
         return jjStartNfaWithStates_0(0, 26, 61);
      case 47:
         return jjStartNfaWithStates_0(0, 28, 41);
      case 59:
         return jjStopAtPos(0, 32);
      case 60:
         jjmatchedKind = 21;
         return jjMoveStringLiteralDfa1_0(0x800000L);
      case 61:
         return jjStopAtPos(0, 19);
      case 62:
         jjmatchedKind = 22;
         return jjMoveStringLiteralDfa1_0(0x1000000L);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private final int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 61:
         if ((active0 & 0x100000L) != 0L)
            return jjStopAtPos(1, 20);
         else if ((active0 & 0x800000L) != 0L)
            return jjStopAtPos(1, 23);
         else if ((active0 & 0x1000000L) != 0L)
            return jjStopAtPos(1, 24);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 109;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 61:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  else if (curChar == 46)
                     jjCheckNAdd(87);
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(82, 83);
                  else if (curChar == 46)
                     jjCheckNAdd(62);
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(74, 75);
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 9)
                        kind = 9;
                     jjCheckNAddTwoStates(70, 71);
                  }
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(66, 67);
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(6, 8);
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 8)
                        kind = 8;
                     jjCheckNAdd(53);
                  }
                  break;
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 8)
                        kind = 8;
                     jjCheckNAddStates(9, 26);
                  }
                  else if ((0x280000000000L & l) != 0L)
                     jjCheckNAddStates(27, 36);
                  else if (curChar == 46)
                     jjCheckNAddTwoStates(62, 87);
                  else if (curChar == 47)
                     jjAddStates(37, 38);
                  else if (curChar == 34)
                     jjCheckNAddStates(39, 41);
                  if (curChar == 46)
                  {
                     if (kind > 15)
                        kind = 15;
                     jjCheckNAddTwoStates(10, 11);
                  }
                  break;
               case 41:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(47, 48);
                  else if (curChar == 47)
                     jjCheckNAddStates(42, 44);
                  break;
               case 1:
                  if ((0xfffffffbffffdbffL & l) != 0L)
                     jjCheckNAddStates(39, 41);
                  break;
               case 3:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAddStates(39, 41);
                  break;
               case 4:
                  if (curChar == 34 && kind > 11)
                     kind = 11;
                  break;
               case 5:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(45, 48);
                  break;
               case 6:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(39, 41);
                  break;
               case 7:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 8:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAdd(6);
                  break;
               case 9:
                  if (curChar != 46)
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjCheckNAddTwoStates(10, 11);
                  break;
               case 10:
                  if ((0x3ff400000000000L & l) == 0L)
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjCheckNAddTwoStates(10, 11);
                  break;
               case 11:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 12;
                  break;
               case 12:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 13:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 14;
                  break;
               case 14:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 15;
                  break;
               case 15:
                  if (curChar == 45)
                     jjstateSet[jjnewStateCnt++] = 16;
                  break;
               case 16:
                  if ((0x3000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 17;
                  break;
               case 17:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjCheckNAddStates(49, 51);
                  break;
               case 18:
                  if (curChar == 45)
                     jjstateSet[jjnewStateCnt++] = 19;
                  break;
               case 19:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 20;
                  break;
               case 20:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjCheckNAddTwoStates(10, 11);
                  break;
               case 40:
                  if (curChar == 47)
                     jjAddStates(37, 38);
                  break;
               case 42:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddStates(42, 44);
                  break;
               case 43:
                  if ((0x2400L & l) != 0L && kind > 5)
                     kind = 5;
                  break;
               case 44:
                  if (curChar == 10 && kind > 5)
                     kind = 5;
                  break;
               case 45:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 44;
                  break;
               case 46:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(47, 48);
                  break;
               case 47:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(47, 48);
                  break;
               case 48:
                  if (curChar == 42)
                     jjAddStates(52, 53);
                  break;
               case 49:
                  if ((0xffff7fffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(50, 48);
                  break;
               case 50:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(50, 48);
                  break;
               case 51:
                  if (curChar == 47 && kind > 6)
                     kind = 6;
                  break;
               case 52:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAddStates(27, 36);
                  break;
               case 53:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 8)
                     kind = 8;
                  jjCheckNAdd(53);
                  break;
               case 54:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(6, 8);
                  break;
               case 55:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(54);
                  break;
               case 56:
                  if (curChar != 46)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(57, 58);
                  break;
               case 57:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(57, 58);
                  break;
               case 59:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(60);
                  break;
               case 60:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(60);
                  break;
               case 62:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(62, 63);
                  break;
               case 64:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(65);
                  break;
               case 65:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(65);
                  break;
               case 66:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(66, 67);
                  break;
               case 68:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(69);
                  break;
               case 69:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(69);
                  break;
               case 70:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(70, 71);
                  break;
               case 72:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(73);
                  break;
               case 73:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(73);
                  break;
               case 74:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(74, 75);
                  break;
               case 78:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 80:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(81);
                  break;
               case 81:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(81, 75);
                  break;
               case 82:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(82, 83);
                  break;
               case 84:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(85);
                  break;
               case 85:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(85, 75);
                  break;
               case 86:
                  if (curChar == 46)
                     jjCheckNAdd(87);
                  break;
               case 87:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(54, 56);
                  break;
               case 89:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(90);
                  break;
               case 90:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(90, 75);
                  break;
               case 91:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 92:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(91);
                  break;
               case 93:
                  if (curChar == 46)
                     jjCheckNAddStates(57, 59);
                  break;
               case 94:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(57, 59);
                  break;
               case 96:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(97);
                  break;
               case 97:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(97, 75);
                  break;
               case 98:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 8)
                     kind = 8;
                  jjCheckNAddStates(9, 26);
                  break;
               case 99:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 100;
                  break;
               case 100:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 101;
                  break;
               case 101:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 102;
                  break;
               case 102:
                  if (curChar == 45)
                     jjstateSet[jjnewStateCnt++] = 103;
                  break;
               case 103:
                  if ((0x3000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 104;
                  break;
               case 104:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 18)
                     kind = 18;
                  jjstateSet[jjnewStateCnt++] = 105;
                  break;
               case 105:
                  if (curChar == 45)
                     jjstateSet[jjnewStateCnt++] = 106;
                  break;
               case 106:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 107;
                  break;
               case 107:
                  if ((0x3ff000000000000L & l) != 0L && kind > 18)
                     kind = 18;
                  break;
               case 108:
                  if (curChar == 46)
                     jjCheckNAddTwoStates(62, 87);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 15)
                        kind = 15;
                     jjCheckNAddTwoStates(10, 11);
                  }
                  if ((0x4000000040L & l) != 0L)
                     jjAddStates(60, 61);
                  else if ((0x10000000100000L & l) != 0L)
                     jjAddStates(62, 63);
                  else if ((0x400000004000L & l) != 0L)
                     jjAddStates(64, 65);
                  break;
               case 1:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(39, 41);
                  break;
               case 2:
                  if (curChar == 92)
                     jjAddStates(66, 68);
                  break;
               case 3:
                  if ((0x14404410144044L & l) != 0L)
                     jjCheckNAddStates(39, 41);
                  break;
               case 9:
               case 10:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjCheckNAddTwoStates(10, 11);
                  break;
               case 21:
                  if ((0x400000004000L & l) != 0L)
                     jjAddStates(64, 65);
                  break;
               case 22:
                  if ((0x100000001000L & l) != 0L && kind > 14)
                     kind = 14;
                  break;
               case 23:
               case 25:
                  if ((0x100000001000L & l) != 0L)
                     jjCheckNAdd(22);
                  break;
               case 24:
                  if ((0x20000000200000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 23;
                  break;
               case 26:
                  if ((0x20000000200000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 25;
                  break;
               case 27:
                  if ((0x10000000100000L & l) != 0L)
                     jjAddStates(62, 63);
                  break;
               case 28:
                  if ((0x2000000020L & l) != 0L && kind > 7)
                     kind = 7;
                  break;
               case 29:
               case 31:
                  if ((0x20000000200000L & l) != 0L)
                     jjCheckNAdd(28);
                  break;
               case 30:
                  if ((0x4000000040000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 29;
                  break;
               case 32:
                  if ((0x4000000040000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 31;
                  break;
               case 33:
                  if ((0x4000000040L & l) != 0L)
                     jjAddStates(60, 61);
                  break;
               case 34:
               case 37:
                  if ((0x8000000080000L & l) != 0L)
                     jjCheckNAdd(28);
                  break;
               case 35:
                  if ((0x100000001000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 34;
                  break;
               case 36:
                  if ((0x200000002L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 35;
                  break;
               case 38:
                  if ((0x100000001000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 37;
                  break;
               case 39:
                  if ((0x200000002L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 38;
                  break;
               case 42:
                  jjAddStates(42, 44);
                  break;
               case 47:
                  jjCheckNAddTwoStates(47, 48);
                  break;
               case 49:
               case 50:
                  jjCheckNAddTwoStates(50, 48);
                  break;
               case 58:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(69, 70);
                  break;
               case 63:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(71, 72);
                  break;
               case 67:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(73, 74);
                  break;
               case 71:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(75, 76);
                  break;
               case 75:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 12)
                     kind = 12;
                  jjstateSet[jjnewStateCnt++] = 76;
                  break;
               case 76:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 12)
                     kind = 12;
                  jjstateSet[jjnewStateCnt++] = 77;
                  break;
               case 77:
                  if ((0x7fffffe07fffffeL & l) != 0L && kind > 12)
                     kind = 12;
                  break;
               case 79:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(77, 78);
                  break;
               case 83:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(79, 80);
                  break;
               case 88:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(81, 82);
                  break;
               case 95:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(83, 84);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(39, 41);
                  break;
               case 42:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(42, 44);
                  break;
               case 47:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(47, 48);
                  break;
               case 49:
               case 50:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(50, 48);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 109 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   92, 91, 93, 78, 79, 75, 55, 54, 56, 53, 55, 54, 56, 66, 67, 70, 
   71, 74, 78, 79, 82, 83, 92, 91, 93, 75, 99, 53, 54, 61, 66, 70, 
   74, 78, 82, 86, 91, 41, 46, 1, 2, 4, 42, 43, 45, 1, 2, 6, 
   4, 10, 11, 18, 49, 51, 87, 88, 75, 94, 95, 75, 36, 39, 30, 32, 
   24, 26, 3, 5, 7, 59, 60, 64, 65, 68, 69, 72, 73, 80, 81, 84, 
   85, 89, 90, 96, 97, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default : 
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, "\75", "\41\75", "\74", "\76", "\74\75", 
"\76\75", "\53", "\55", "\52", "\57", "\41", "\50", "\51", "\73", };
public static final String[] lexStateNames = {
   "DEFAULT", 
};
static final long[] jjtoToken = {
   0x1fffcdb81L, 
};
static final long[] jjtoSkip = {
   0x7eL, 
};
protected JavaCharStream input_stream;
private final int[] jjrounds = new int[109];
private final int[] jjstateSet = new int[218];
protected char curChar;
public FlParserTokenManager(JavaCharStream stream)
{
   if (JavaCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public FlParserTokenManager(JavaCharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(JavaCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 109; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(JavaCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public Token getNextToken() 
{
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}
