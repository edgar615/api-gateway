package com.edgar.direwolves.discovery.consul;

import com.edgar.direwolves.discovery.ServiceDiscoveryInternal;
import com.edgar.direwolves.discovery.ServiceInstance;

import java.util.Objects;

public class ImportedConsulService {

  private final String name;
  private final ServiceInstance instance;
  private final String id;

  /**
   * Creates a new instance of {@link ImportedConsulService}.
   *
   * @param name     the service name
   * @param id       the service id, may be the name
   * @param instance the instance
   */
  public ImportedConsulService(String name, String id, ServiceInstance instance) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(id);
    Objects.requireNonNull(instance);
    this.name = name;
    this.instance = instance;
    this.id = id;
  }

  /**
   * @return the name
   */
  public String name() {
    return name;
  }

  /**
   * Registers the service and completes the given future when done.
   *
   * @param serviceDiscovery the service publisher instance
   * @return the current {@link ImportedConsulService}
   */
  public ImportedConsulService register(ServiceDiscoveryInternal serviceDiscovery) {
    serviceDiscovery.publish(instance);
    return this;
  }

  /**
   * Unregisters the service and completes the given future when done, if not {@code null}
   *
   * @param serviceDiscovery the service publisher instance
   */
  public void unregister(ServiceDiscoveryInternal serviceDiscovery) {
    serviceDiscovery.unpublish(instance.id());
  }

  /**
   * @return the id
   */
  String id() {
    return id;
  }
}