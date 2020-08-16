# EEPROM-Builder
A Compiler for a MicroCode interpreter 


## Overview

This repo contains code for a compiler that generates the code for a MicroCode interpreter. This allows 
a developer to write their own assembly language.

This code application is designed as a replacement for the Arduino code Ben Eater used in his 8-bit Computer design.
(see https://eater.net/8bit)

While Ben's original design is a wonderful way to learn how create a CPU, if we want to start playing with the
hardware design then we have to keep rewriting Arduino code that generates the EEPROMs. If something 
fails then it is not immediately obvious if it's a hardware problem or a problem with the EEPROM code.

My solution was to write this compiler. Using 4 simple source files it is capable of generating the code
for the EEPROM.

## Source Files
### project.txt

This file describes, at a high level the purpose of the project and the configuration of the hardware design. The 
sections in this file are:

* Overview - Settings that name and version the project
* Hardware - Settings that describe the hardware
* Description - Optional meta data that is added to each generated EEPROM file 

### input_pins.txt

This file describes the wiring of the Address Lines that are common to all EEPROMs in the system. The sections 
in this file are:

* M_CYCLE - Machine Cycle Counter
* IR - Instruction Register
* FLAG - Each of the Flags

### output_pins.txt

This file describes the wiring of the Data Lines from each of the EEPROMs. The sections in this file are the names
of the EEPROM. By default each output pin is active high - that is the compiler will only set the PIN high when the 
code says that it should be trigger. However, by prefixing the pin name with a forwards slash ('/') the compiler will
generate code where the pin is active low.   

### code.txt

This file describes the assembly language instructions. The sections in this file are:

* fetch - A required section that describes the pins that need to be asserted for the 'fetch' cycle. This is normally
            used to load the next instruction into the Instruction Register and increment the Program Counter 
* final - An optional section that describes any additional pins that need to be asserted at the end of each 
            instruction. Some hardware designs could use this to reset the M-Cycle counter
* '*' - An optional section that describes if additional pins that should be asserted if the CPU attempts to process 
            an undefined instruction or state. This could be used to halt the system clock                    
* Named instructions - These sections contain the pins that need to be asserted to complete each instruction. The 
            Fetch cycle is automatically prepended. In addition the 'final' section will be merged into the final 
            step of the instruction.
            
Each named instruction will have at least one condition associated with it - the state of the Instruction Register. 
The state of the flags can be used for additional conditions. Instructions such as JZ (Jump if Zero) that have
additional condition(s) (in this case that the Zero flag is set) require a variant that is executed if the flag is 
not set. Developers are free to implement their own versions, however if the compiler notices that a variant has not
been defined then the compiler will automatically generate a no-operation instruction.

**Note:** The indenting of this file is important - conditions are aligned to the left margin. Instructions are
indented by tabs and/or spaces. 


## Generated Files

The generated files are:

* AvailableCodes.txt - A list of unused OpCodes. This is handy if you want to known where you can add a new instruction
* Eeprom_map.txt - A file that shows which data pins should be asserted for every of the CPU. this is used for Debugging
* Instructions.txt - A list of instructions and the OpCodes they are assigned to. This is used for writing programs. 
* OpCodes.txt - A list of defined OpCode and the instructions assigned to them  
* out.log - The Compiler output
* Rom_.srec - The image for each of the EEPROM Chips. These are in SRec format as they contain additional metadata.
    Commercial EEPROM programmers should support this format, however if you want to use Ben Eaters design for an
    EEPROM Programmer then you could use my EEPROM Programmer software (https://github.com/TymeFly/BEEP)


## Sample Projects

* ben-eater - The code to compile the EEPROMs for an unmodified version of Ben Eaters hardware design.

**Note:** In Ben's original design the same code was programmed into two identical EEPROMs. A hardware link was used
to determine what the data pins should do. This compiler can not generate a single file for multiple EEPPROMs, however
should be less of an issue as you don't have to write two Arduino programs to generate them. It also leave an extra
address line available for addition CPU states (extra flags, larger OpCodes or more MCycles for complex instructions)   

* template - A sample project that describes the function of each of the input files in detail.
	
	
## External Libraries

* My own S-Rec library for reading/writing S-Records files (https://github.com/TymeFly/S-Rec)
* JSR305 for documenting null object references
* args4j for handling the CLI parsing
