package com.carracer.infrastructure.network.models.Requests;

public class DeleteRequest {
    private String Id;
    public DeleteRequest(String id) {
        this.Id = id;
    }
    public String getId() {
        return Id;
    }
}
