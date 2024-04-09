SYSC 3303 Group 8 Scheduler

Setup Intruction: 
in main set the scheduler port number and infor port number to what was agreed upon by the out system. 
then change the port numbers and ip address to match what the elevators are running on. 

File Descriptions:
ElevatorInfo: Constructed from packets sent between Scheduler and Elevator, meant for Elevator to update the Scheduler on its status
ElevatorInfoTest: Tests to check if the methods to convert to packet and back work
ElevatorSchedulerData: Information that the Scheduler knows about the Elevators
Main: Starts Scheduler and configures which elevator's its recognizes
Request: Constructed from packets sent between Floor Scheduler and Elevator
RequestWrapper: Holds both Request and Elevator that the Request was sent to
SchedulerScheduler: The code for the Scheduler
SchedulerSubsystemTest: Code to test scheduler methods

Responsibilities:
Alex Tempel was mostly responsible for creating the scheduler.
Peter Willis was responsible for the code that selects which elevator to send the requests to
Nick Nemec was responsible for the console of the status of the elevators
