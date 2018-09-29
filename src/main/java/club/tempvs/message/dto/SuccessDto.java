package club.tempvs.message.dto;

public class SuccessDto {

    private Boolean isSuccess;

    public SuccessDto() {

    }

    public SuccessDto(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public Boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }
}
