import React, { Component } from 'react';

import BASE_URL from '../../Constants';

import { Link } from 'react-router-dom';


import Form from 'react-bootstrap/Form';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';

import { withRouter } from 'react-router-dom';

import './Wallet.css';

class Wallet extends Component {

    controller = new AbortController();

    constructor(props) {
        super(props);
        this.state = {
            wallet: null
        }
    }

    componentDidMount() {
        this.fetchRandomWallet();
    }

    componentWillUnmount() {
        this.controller.abort();
    }

    async fetchRandomWallet() {
        await fetch('http://localhost:8080/wallet', {
            signal: this.controller.signal,
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(wallet => {
            this.setState({ wallet });
        });
    }

    render() {

        const { wallet } = this.state;

        if (wallet != null) {
            return (
                <div className="wallet">
                    <Form className="block-form">
                        <Form.Group as={Row} controlId="formPlaintextIndex">
                            <Form.Label column sm="3">
                                <span className="tx-span"> Private key: </span>
                            </Form.Label>
                            <Col sm="9">
                                {wallet.privateKey}
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} controlId="formPlaintextIndex">
                            <Form.Label column sm="3">
                                <span className="tx-span"> Public key: </span>
                            </Form.Label>
                            <Col sm="9">
                                {wallet.publicKey}
                            </Col>
                        </Form.Group>
                    </Form>
                </div>
            );
        }
        else return (
            <div>LOADING</div>
        );
    }
}

export default withRouter(Wallet);