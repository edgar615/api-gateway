package com.github.edgar615.gateway.mock;

import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/9/7.
 *
 * @author Edgar  Date 2017/9/7
 */
@Deprecated
public class DeviceStore {
  private final List<Device> devices = new CopyOnWriteArrayList<>();

  public void add(Device device) {
    boolean exists = devices.stream()
            .anyMatch(d -> d.getId() == device.getId());
    if (exists) {
      throw SystemException.create(DefaultErrorCode.ALREADY_EXISTS);
    }
    devices.add(device);
  }

  public void update(Device device) {
    devices.removeIf(d -> d.getId() == device.getId());
    devices.add(device);
  }

  public Device get(int id) {
    Optional<Device> optional = devices.stream()
            .filter(d -> d.getId() == id)
            .findFirst();
    if (optional.isPresent()) {
      return optional.get();
    }
    throw SystemException.create(DefaultErrorCode.ALREADY_EXISTS);
  }

  public void delete(int id) {
    devices.removeIf(d -> d.getId() == id);
  }

  public List<Device> query(int start, int offset, Integer id, String name) {
    return devices.stream()
            .filter(d -> {
              boolean filter = true;
              if (id != null) {
                filter = id.equals(d.getId());
              }
              if (name != null) {
                filter = name.equals(d.getName());
              }
              return filter;
            }).collect(Collectors.toList())
            .subList(start, start + offset);
  }

  public void clear() {
    devices.clear();
  }

}
