// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
//************************************************************************
/**
 * Class definitions for FastOS_UNIX_Application.
 *
 * @author  Div, Oivind H. Danielsen
 */

#pragma once


#include "types.h"
#include "app.h"

class FastOS_UNIX_ProcessStarter;
class FastOS_UNIX_IPCHelper;
class FastOS_UNIX_Process;

/**
 * This is the generic UNIX implementation of @ref FastOS_ApplicationInterface
 */
class FastOS_UNIX_Application : public FastOS_ApplicationInterface
{
private:
    FastOS_UNIX_Application(const FastOS_UNIX_Application&);
    FastOS_UNIX_Application& operator=(const FastOS_UNIX_Application&);

    FastOS_UNIX_ProcessStarter *_processStarter;
    FastOS_UNIX_IPCHelper *_ipcHelper;

protected:
    bool PreThreadInit () override;
public:
    FastOS_UNIX_Application ();
    virtual ~FastOS_UNIX_Application();

    int GetOpt (const char *optionsString, const char* &optionArgument, int &optionIndex);

    int GetOptLong(const char *optionsString, const char* &optionArgument, int &optionIndex,
                   const struct option *longopts, int *longindex);

    static unsigned int GetCurrentProcessId ();

    FastOS_UNIX_ProcessStarter *GetProcessStarter ();
    bool Init () override;
    void Cleanup () override;
    bool SendParentIPCMessage (const void *data, size_t length) override;
    bool SendIPCMessage (FastOS_UNIX_Process *xproc, const void *buffer, int length);
    void AddToIPCComm (FastOS_UNIX_Process *process);
    void RemoveFromIPCComm (FastOS_UNIX_Process *process);
};


