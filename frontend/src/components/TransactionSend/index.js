import React, {Component} from 'react';

import {BASE_URL} from '../../Constants';

import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';

import { Link } from 'react-router-dom';

import { withRouter } from 'react-router-dom';

import './TxSend.css';

class TransactionSend extends Component {
    constructor(props) {
        super(props);
        this.state = {
            transactionPayload: {
                from: "",
                to: "",
                value: 0
            }
        }
    }

    handleChange(event) {
        let transactionPayload = { ...this.state.transactionPayload};
        transactionPayload[event.target.name] = event.target.value;
        this.setState({ transactionPayload });
    }

    async handleSubmit(event) {
        event.preventDefault();
        const { transactionPayload } = this.state;
        await fetch(BASE_URL + 'transaction',
            {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(transactionPayload)
            }
        ).then(() => {this.props.history.push("/");});
    }

    render() {
        return (
            <div className="login-container">
                <Form onSubmit={this.handleSubmit.bind(this)}>
                    <h2>Send a transaction</h2>
                    <Form.Group>
                        <Form.Control placeholder="Private key sender: " name="from" onChange={this.handleChange.bind(this)} />
                    </Form.Group>
                    <Form.Group>
                        <Form.Control placeholder="Public key receiver: " name="to" onChange={this.handleChange.bind(this)} />
                    </Form.Group>
                    <Form.Group>
                        <Form.Control placeholder="value" name="value" onChange={this.handleChange.bind(this)} />
                    </Form.Group>
                    <Form.Group>
                        <Button variant="primary" className="btn btn-success btn-lg btn-block signup-btn" type="submit">
                            Submit
                        </Button>
                    </Form.Group>
                </Form>
                <Link to="/"> Cancel </Link>
            </div>
        );
    }
}

export default withRouter(TransactionSend);