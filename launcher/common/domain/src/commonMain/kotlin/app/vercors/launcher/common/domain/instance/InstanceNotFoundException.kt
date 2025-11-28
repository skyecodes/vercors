package app.vercors.launcher.common.domain.instance

class InstanceNotFoundException(instanceId: String) :
    IllegalArgumentException("Instance with id '$instanceId' not found")