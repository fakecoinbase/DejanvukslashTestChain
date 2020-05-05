import React, { Component } from 'react';

import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import InputGroup from 'react-bootstrap/InputGroup';
import FormControl from 'react-bootstrap/FormControl';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Dropdown from 'react-bootstrap/Dropdown';
import ListGroup from 'react-bootstrap/ListGroup';
import Table from 'react-bootstrap/Table';
import { Link } from 'react-router-dom';

import Transaction from '../Transaction';

import './Explorer.css';

class Explorer extends Component {
    constructor(props) {
        super(props);
        this.state = {
            blocks: [],
            txs: [],
            difficulty: 0,
            price: 0,
            mode: 'blocks' // blocks,transactions,difficulty,price
        }
    }

    async componentWillMount() {
        await fetch('http://localhost:8080/' + 'block', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(blocks => {
            this.setState({ blocks });
        });
        
        await fetch('http://localhost:8080/' + 'transaction', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(txs => {
            this.setState({ txs });
        });
    }

    handleClick(name) {
        let { mode } = this.state;
        mode = name;
        this.setState({ mode });
    }

    tableMode() {
        const { blocks, txs, difficulty, price, mode } = this.state;

        if (mode == 'blocks') {
            return (
                <div className="blocks-table">
                    <h5> Latest blocks </h5>
                    <Table responsive>
                        <thead>
                            <tr>
                                <th>Height</th>
                                <th>Hash</th>
                                <th>Timestamp</th>
                                <th>Difficulty</th>
                                <th>Transaction nr</th>
                            </tr>
                        </thead>
                        <tbody>
                            {
                                blocks.slice(-15).reverse().map(block =>
                                    (
                                        <tr key={block.index}>
                                            <td><Link className="" to={"block/" + block.hash}>{block.index}</Link></td>
                                            <td><Link className="" to={"block/" + block.hash}>{block.hash.substring(0, 1) + "..." + block.hash.substring(20, 50) + '...'}</Link></td>
                                            <td>{block.timestamp}</td>
                                            <td>{block.difficultyTarget}</td>
                                            <td>{block.transactions.length}</td>
                                        </tr>
                                    )
                                )
                            }
                        </tbody>
                    </Table>
                </div>
            );
        }
        else if (mode == 'transactions') {

            const txRows = txs.slice(-15).reverse().map(tx =>
                (
                    <Transaction key={tx.txid} {...tx} confirmed={false}></Transaction>
                )
            )

            return (
                <div className="transactions-table">
                    <h5> Transactions </h5>
                    <Link to={"/tx/view"}>View all transactions</Link>
                    { txRows }
                </div>
            );
        }
        else if (mode == 'difficulty') {

        }
        else if (mode == 'price') {

        }
    }


    render() {
        const { blocks, txs, difficulty, price, mode } = this.state;

        return (
            <div className="explorer">
                <div className="container ">
                    <div className="row">
                        <div className="col">
                            <h5 id="explorer-h5">Explorer</h5>
                        </div>
                        <div className="col-9">
                            <Form className="searchbar">
                                <InputGroup className="mb-3">
                                    <FormControl
                                        placeholder="Search ..."
                                        aria-label="Search "
                                        aria-describedby="basic-addon2"
                                    />
                                    <DropdownButton id="dropdown-basic-button" title="Crypto">
                                        <Dropdown.Item href="#/action-1">Testcoin</Dropdown.Item>
                                        <Dropdown.Item href="#/action-2">Bitcoin</Dropdown.Item>
                                        <Dropdown.Item href="#/action-3">Ethereum</Dropdown.Item>
                                    </DropdownButton>
                                    <Button id="search-button" variant="outline-secondary"> Search </Button>
                                </InputGroup>
                            </Form>

                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <h5> Bitcoin </h5>
                            <ListGroup>
                                <ListGroup.Item action active={mode == 'blocks'} name="blocks" onClick={this.handleClick.bind(this, 'blocks')} >Blocks</ListGroup.Item>
                                <ListGroup.Item action active={mode == 'transactions'} name="transactions" onClick={this.handleClick.bind(this, 'transactions')} >Transactions</ListGroup.Item>
                                <ListGroup.Item action active={mode == 'difficulty'} name="difficulty" onClick={this.handleClick.bind(this, 'difficulty')} >Difficulty</ListGroup.Item>
                                <ListGroup.Item action active={mode == 'price'} name="price" onClick={this.handleClick.bind(this, 'price')} >Price</ListGroup.Item>
                            </ListGroup>
                        </div>
                        <div className="col-9">
                            {this.tableMode()}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default Explorer;