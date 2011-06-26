// Agent leader in project cowboy-agents

/* Initial beliefs and rules */
// I want to play "leader" in "capture"
desired_role(capture,leader).

// I want to commit to "m2" mission in "catchCows" schemes
desired_mission(catchCowScheme,m2).

// include common plans for MOISE+ agents
{ include("common.asl") }
{ include("moving.asl") }

/* Initial goals */
!create_group. // initial goal
!connect_to_server.	// I want to connect to the server

// create a group to the team
+!create_group : true 
   <- jmoise.create_group(gr_name(team),team);
	  .print("Group ", "team" ," created");
	  jmoise.create_group(capture,team);
	  .print("Sub-group ","capture"," created");
	  jmoise.create_group(sabotage,team);
	  .print("Sub-group ","sabotage"," created").
-!create_group[error_msg(M),code(C),code_line(L)]
   <- .print("Error creating group, command: ",C,", line ",L,", message: ",M).

/* Functional events */
// when a scheme has finished, start another
/*
-scheme(catchCowScheme,_)
	: group(team,GId)
	<- jmoise.create_scheme(catchCowScheme, [GId], SchId);
	.print("Scheme ",SchId," created").
*/



/* Plans */

// when authenticated and playing role "leader",
// create a catchCow scheme 
+auth(ok): play(Me,leader,GId) & .my_name(Me)
	<- jmoise.create_scheme(catchCowScheme,[GId]);
	.print("Scheme created").
	
+sim_start(SimId): true
	<- .print("Simulation started").


// goal: coordinate cowboys
{ begin maintenance_goal("+pos(_,_,_)") }

+!coordinate_cowboys : pos(X,Y,_) & .my_name(Me) & scheme_group(Sch,G) &
	not .intend(coordinate_cowboys) &
	jia.preferable_cluster(X,Y,L,S,N) &
	.list(L) &
	.length(L) > 0 &
	L = [pos(ClX,ClY),_] &
	.count(play(_,captor,G),NAg) &
	jia.position_to_cluster(ClX,ClY,NAg,Formation)
  	<-	.findall(P, play(P,captor,G), Agents);
  	   	//.print("Formation : ",Formation);
  	   	//.print("Agents : ",Agents);
  	   	.print("New formation!!");
  	   	!send_target(Agents,Formation);
  	   	!go_open_fence.

+!coordinate_cowboys 
   	<-  !go_open_fence.

-!coordinate_cowboys[error_msg(M),code(C),code_line(L)]
	<- .print("Error on coordinate_cowboys, command: ",C,", line ",L,", message: ",M).

{ end }

// send a new target(X,Y) to the agents
+!send_target(Agents,[pos(X,Y)|TLoc])
 	<- !find_closest(Agents,pos(X,Y),NearAg);
 	   .send(NearAg,tell,target(X,Y));
 	   .delete(NearAg,Agents,TAg);
 	   !send_target(TAg,TLoc).
+!send_target([],[]).
 
 // find the agent near to pos(FX,FY)
+!find_closest(Agents, pos(FX,FY), NearAg)
 	<- .my_name(Me);
 	   .findall(d(D,Ag),
           .member(Ag,Agents) & ally_pos(Ag,AgX,AgY) & jia.path_length(FX,FY,AgX,AgY,D),
                            Distances);
           .min(Distances,d(_,NearAg)).
-!find_closest[error_msg(M),code(C),code_line(L)]
	<- .print("Error on find_closest, command: ",C,", line ",L,", message: ",M).


// open fence
+!go_open_fence
	: switch(SX,SY) & jia.is_corral_switch(SX,SY) &
	  pos(X,Y,ActionId) &
	  not .intend(pos(_,_)) &
	  not has_no_fence
	<-  !fence_as_obstacle(0);
		jia.switch_places(SX,SY,X,Y,PX,PY,_,_);
		if ( not (X == PX & Y == PY)) {
			.print("go open fence");
			jia.direction_cow_not_obstacle(X, Y, PX, PY, D);
			.print("[action: ",ActionId,"] from ",X,"x",Y," to ", PX,"x",PY," -> ",D);
			-+last_dir(D);
			do(D,ActionId)
		}.

+!go_open_fence
	: corral_center(CX,CY) &
	  not (switch(X,Y) & jia.is_corral_switch(X,Y)) &
	  not has_no_fence
	<-  !fence_as_obstacle(0);
		.print("go to corral");
		!pos(CX,CY).

+!go_open_fence
	: not switch(SX,SY) & corral_center(CX,CY) &
	  not has_no_fence
	<- 	!fence_as_obstacle(0);
		.print("no swith, go to corral");
		!pos(CX,CY).

+!go_open_fence
	: pos(X,Y,ActionId) & corral_center(CX,CY) &
	  jia.dist(X,Y,CX,CY, Dist) &
	  Dist < 3
	<-  -+has_no_fence.

+!go_open_fence
	: has_not_fence & pos(X,Y,ActionId) &
	  corral(XO,YO,XI,YI)
	<-  !pos(XO,YO).

+!go_open_fence
	: has_not_fence & pos(X,Y,ActionId) &
	  corral(X,Y,XI,YI).

+!go_open_fence : not pos(_,_,_)
   	<-  .wait({+pos(_,_,_)}).


// end of simulation
@fimdesimulacao[atomic]
+end_of_simulation(_Result)
  <- -end_of_simulation(_);
     .drop_all_desires;
     !remove_org.

// remove all groups and schemes (only leader does that)
+!remove_org
   : .my_name(leader)
  <- .print("Removing all groups and schemes");
     if(group(team,Old)) {
        jmoise.remove_group(Old)
     };
     for( scheme(_,SchId) ) {
        jmoise.remove_scheme(SchId)
     }.

+!remove_org.