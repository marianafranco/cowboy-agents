/* plans for moving */

last_dir(null). // the last movement
fence_obstacle(fences).


+?pos(X, Y, ActionId)
  <- .wait({+pos(X,Y,ActionId)}).

/* next_step: do an action towards some destination, 
   the destination may be unachievable */
+!next_step(X,Y)
   	:  pos(AgX,AgY,ActionId) & fence_obstacle(FO)
   	<- 	jia.direction(AgX, AgY, X, Y, D, FO);
      	.print("[action: ",ActionId,"] from ",AgX,"x",AgY," to ", X,"x",Y," -> ",D);
      	-+last_dir(D);
      	do(D,ActionId).
//+!next_step(X,Y) : not pos(_,_,_) // I still do not know my position
//   <- !next_step(X,Y).

-!next_step(X,Y) // failure handling -> start again!
   <- .print("Failed next_step to ", X,"x",Y," fixing and trying again!");
      -+last_dir(null);
      !next_step(X,Y).


/* pos is used when it is always possible to go */
+!pos(X,Y) 
  :  .desire(spos(OX,OY))
  <- .current_intention(I);
     .print("** Trying to go to ",X,",",Y," while another !pos to ",OX,",",OY," is running by intention ",I);
      .fail.
+!pos(X,Y)
  <- jia.set_target(X,Y); 
     !spos(X,Y).
     
  
+!spos(X,Y) : pos(X,Y,ActionId). // <- .print("I've reached ",X,"x",Y).
+!spos(X,Y) : not jia.obstacle(X,Y) // the obstacle may be discovered after !pos(X,Y), so spos should fail.
  <- !next_step(X,Y).




/* -- plans to move to a destination represented in the belief target(X,Y) 
   -- (it is a kind of persistent goal)
*/

// if the target is changed, "restart" move
+target(NX,NY)
  <- jia.set_target(NX,NY);
     //.print("Adding/Changing the target to (",NX,",",NY,")!");
     .drop_desire(move);
     !move.

+target(X,Y) : pos(X,Y,ActionId). // <- .print("I've reached ",X,"x",Y).

+!move : target(X,Y) & jia.obstacle(X,Y) & // the target is an obstacle!
	pos(AgX,AgY,ActionId)
  	<-  //.print("My target ", X, ",", Y, " is an obstacle, ignoring target!");
     	do(skip,ActionId).
     	//!move.

// does one step towards target  
+!move
   : pos(X,Y,ActionId) & 
     target(BX,BY) & 
     fence_obstacle(FO) &
     jia.direction(X, Y, BX, BY, D, FO) // jia.direction finds one action D (using A*) towards the target
  <- .print("[action: ",ActionId,"] moving to target ", BX, ",", BY, " -> ",D);
  	 do(D,ActionId).  // this action will "block" the intention until it is sent to the simulator (in the end of the cycle)
     //!move. // continue moving

+!move.

// in case of failure, move
-!move
  <- .current_intention(I);
     .println("failure in moving; intention was: ",I);
     !move.

// set fence as obstacle for N cycles
+!fence_as_obstacle(N) : N <= 0
  <- -+fence_obstacle(no).
   
+!fence_as_obstacle(N) : N > 0
  <- -+fence_obstacle(fences);
     .wait( { +pos(_,_,_) } );
     !!fence_as_obstacle(N-1).
       
-!fence_as_obstacle(_) 
  <- -+fence_obstacle(no).