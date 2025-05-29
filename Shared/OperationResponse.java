package Shared;

public class OperationResponse {
    private int response;
    private String message;

    public OperationResponse(Integer Code, String Operation) {
        switch (Operation) {
            case "register":
                switch (Code) {
                    case 100:
                        this.message = "User succesfully registered!";
                        break;

                    case 101:
                        this.message = "invalid password";
                        break;

                    case 102:
                        this.message = "username not available";
                        break;

                    case 103:
                        this.message = "something gone wrong";
                        break;
                }
                break;
            case "updateCredentials":
                switch (Code) {
                    case 100:
                        this.message = "Credentials succesfully updated!";
                        break;

                    case 101:
                        this.message = "invalid new password";
                        break;

                    case 102:
                        this.message = "password mismatch/non existent username";
                        break;

                    case 103:
                        this.message = "new password equal to old one";
                        break;

                    case 104:
                        this.message = "user currently logged";
                        break;

                    case 105:
                        this.message = "something gone wrong";
                }
                break;
            case "login":
                switch (Code) {
                    case 100:
                        this.message = "User succesfully logged in!";
                        break;

                    case 101:
                        this.message = "password mismatch/non existent username";
                        break;

                    case 102:
                        this.message = "user already logged in";
                        break;

                    case 103:
                        this.message = "error";
                        break;
                }
                break;
            case "logout":
                switch (Code) {
                    case 100:
                        this.message = "See you!";
                        break;

                    case 101:
                        this.message = "mismatch/non existent/not logged in";
                        break;
                }
                break;
            case "cancelOrder":
                switch (Code) {
                    case 100:
                        this.message = "Order cancelled!";
                        break;

                    case 101:
                        this.message = "order non existent/different user/already finalized";
                        break;
                }
                break;
            case "getPriceHistory":
                switch (Code) {
                    case 100:
                        this.message = "OK";
                        break;

                    case 101:
                        this.message = "Error 101: wrong month format (MMMYYYY)";
                        break;

                    case 102:
                        this.message = "Error 102: no orders for the choosen period";
                        break;
                }
                break;
        }

        this.response = Code;
    }

    public int getResponse() {
        return this.response;
    }

    public void setResponse(int Response) {
        this.response = Response;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String Message) {
        this.message = Message;
    }
}