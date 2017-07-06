package com.edgar.servicediscovery.zookeeper;

import io.vertx.core.Future;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.spi.ServicePublisher;

import java.util.Objects;

/**
 * 仿造ImportedConsulService实现
 */
public class ImportedZookeeperService {

  private final String name;

  private final Record record;

  private final String id;

  /**
   * Creates a new instance of {@link ImportedZookeeperService}.
   *
   * @param name   the service name
   * @param id     the service id, may be the name
   * @param record the record (not yet registered)
   */
  public ImportedZookeeperService(String name, String id, Record record) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(id);
    Objects.requireNonNull(record);
    this.name = name;
    this.record = record;
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
   * @param publisher  the service publisher instance
   * @param completion the completion future
   * @return the current {@link ImportedZookeeperService}
   */
  public ImportedZookeeperService register(ServicePublisher publisher,
                                           Future<ImportedZookeeperService> completion) {
    publisher.publish(record, ar -> {
      if (ar.succeeded()) {
        record.setRegistration(ar.result().getRegistration());
        completion.complete(this);
      } else {
        completion.fail(ar.cause());
      }
    });
    return this;
  }

  /**
   * Unregisters the service and completes the given future when done, if not {@code null}
   *
   * @param publiher   the service publisher instance
   * @param completion the completion future
   */
  public void unregister(ServicePublisher publiher, Future<Void> completion) {
    if (record.getRegistration() != null) {
      publiher.unpublish(record.getRegistration(), ar -> {
        if (ar.succeeded()) {
          record.setRegistration(null);
        }
        if (completion != null) {
          completion.complete();
        }
      });
    } else {
      if (completion != null) {
        completion.fail("Record not published");
      }
    }
  }

  /**
   * @return the id
   */
  String id() {
    return id;
  }
}
