package architecture;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestArchitecture {

  // Uncomment the annotation below to run the architecture showing components status
  // @Test
  public void testShowComponentes() {

    //a complete test (for visual purposes only).
    //a single code as follows
//		ldi 2
//		store 40
//		ldi -4
//		point:
//		store 41  //mem[41]=-4 (then -3, -2, -1, 0)
//		read 40
//		add 40    //mem[40] + mem[40]
//		store 40  //result must be in 40
//		read 41
//		inc
//		jn point
//		end

    Architecture arch = new Architecture(true);
    arch.getMemory().getDataList()[0] = 7;
    arch.getMemory().getDataList()[1] = 2;
    arch.getMemory().getDataList()[2] = 6;
    arch.getMemory().getDataList()[3] = 40;
    arch.getMemory().getDataList()[4] = 7;
    arch.getMemory().getDataList()[5] = -4;
    arch.getMemory().getDataList()[6] = 6;
    arch.getMemory().getDataList()[7] = 41;
    arch.getMemory().getDataList()[8] = 5;
    arch.getMemory().getDataList()[9] = 40;
    arch.getMemory().getDataList()[10] = 0;
    arch.getMemory().getDataList()[11] = 40;
    arch.getMemory().getDataList()[12] = 6;
    arch.getMemory().getDataList()[13] = 40;
    arch.getMemory().getDataList()[14] = 5;
    arch.getMemory().getDataList()[15] = 41;
    arch.getMemory().getDataList()[16] = 8;
    arch.getMemory().getDataList()[17] = 4;
    arch.getMemory().getDataList()[18] = 6;
    arch.getMemory().getDataList()[19] = -1;
    arch.getMemory().getDataList()[40] = 0;
    arch.getMemory().getDataList()[41] = 0;
    //now the program and the variables are stored. we can run
    arch.controlUnitEexec();
  }
  @Test
  public void testSubRegMem()
  {
    Architecture arch = new Architecture();
    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 12;


    // Now setting the registers values
    arch.getExtbus().put(77);
    arch.getRegistersList().get(0).store(); // RPG0 has 77

    // SUB %rpg0 100
    arch.subRegMem();

    // Testing if m
    // PC was pointing to 30; now it must be pointing to 33
    arch.getPC().internalRead();
    assertEquals(33, arch.getIntbus().get());
  }
  /*
   * The following tests are for the architecture components
   */
  /*
  * in this test we will test the method that stores a value of register in memory
  *
  * */
  @Test
  public void testMoveMemReg() {
    Architecture arch = new Architecture();

    // Making PC point to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // Setting the memory values
    arch.getMemory().getDataList()[31] = 0;
    arch.getMemory().getDataList()[32] = 100;

    // Now setting the registers values
    arch.getExtbus().put(77);
    arch.getRegistersList().get(0).store(); // RPG0 has 77

    // MOVE %rpg0 100
    arch.moveMemReg();

    // Testing if register RPG0 stores the value 100
    arch.getRegistersList().get(0).read();
    arch.getPC().internalRead();
    assertEquals(33, arch.getIntbus().get());


  }
  @Test
  public void testMoveRegReg() {
    Architecture arch = new Architecture();

    // making PC points to position 30
    arch.getIntbus().put(30);
    arch.getPC().internalStore();

    // setting the memory values
    arch.getMemory().getDataList()[31] = 1;
    arch.getMemory().getDataList()[32] = 0;

    // now setting the registers values
    arch.getExtbus().put(77);
    arch.getRegistersList().get(0).store(); //RPG0 has 45
    arch.getExtbus().put(109);
    arch.getRegistersList().get(1).store(); //RPG1 has 99

    // MOVE %rpg1 %rpg0
    arch.moveRegReg();

    // testing if both REG1 and REG0 store the same value: 99
    arch.getRegistersList().get(0).read();
    assertEquals(109, arch.getExtbus().get());
    arch.getRegistersList().get(1).read();
    assertEquals(109, arch.getExtbus().get());

    // Testing if PC points to 3 positions after the original
    // PC was pointing to 30; now it must be pointing to 33
    arch.getPC().internalRead();
    assertEquals(33, arch.getIntbus().get());
  }
}
