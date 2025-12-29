package app.vercors.launcher.common.domain.instance

import app.vercors.launcher.common.domain.instance.model.InstanceId

class InstanceNotFoundException(instanceId: InstanceId) :
    IllegalArgumentException("Instance with ID '$instanceId' not found")
