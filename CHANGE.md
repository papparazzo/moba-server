# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

### [3.0.0] - 2025-12-25
#### Added
- Add SOFTWARE_ERROR and CONNECTION_LOST as a new EmergencyTriggerReason
- Routing added

#### Changed
- Message-system improved

### [2.1.0] - 2025-05-03
#### Fixed
- set propper permissions on IPC-fifo-file 
- KeepAlive-thread added to keep tcp-connection up

#### Changed
- replace dispatcher.broadcast/send with sendGroup/sendSingle/sendAll to enhance message routing


### [2.0.0] - 2025-04-22
#### Fixed
- create IPC-fifo-file if not exists 

#### Changed 
- Exception-Handling refactored

#### Removed
- GUI-Message replaced by Messaging


### [1.7.1] - 2025-04-12
#### Fixed
- Typo fixed (SOFTWARE_MANUEL -> SOFTWARE_MANUEL)
- Null-check in AllowList-class

### [1.7.0] - 2025-04-02
#### Changed
- Logging improved


### [1.6.2] - 2025-04-01
#### Fixed 
- unittest fixed
- check if fifo exists


### [1.6.1] - 2025-03-31
#### Fixed
- version is now converted into a valid string 


### [1.6.0] - 2025-03-30
#### Add
- ipc added to add ip addresses at runtime


### [1.5.0] - 2025-03-29
#### Changed
- get relevant information from a manifest-file


### [1.4.0] - 2025-03-27
#### Added
- command line parameters


### [1.3.1] - 2025-03-26
#### Fixed
- null check on allowlist

### [1.3.0] - 2025-03-25
#### Changed
- exception-handling in Endpoint-class improved
- allow-list added


### [1.2.0] - 2025-03-23
#### Changed
- avoid Thread.sleep() in Acceptor-thread


### [1.1.0] - 2025-03-23
#### Updated
- some smaller improvements (use of records)
- gradle-wrapper updated

#### Changed
- close socket after "max-client" reached


### [1.0.0] - 2025-03-22
#### Added 
- initial release
