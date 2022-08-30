package com.logreposit.logrepositapi.rest.filters.clientinfo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientInfo {
  private String referer;
  private String fullUrl;
  private String ipAddress;
  private String userAgent;
}
